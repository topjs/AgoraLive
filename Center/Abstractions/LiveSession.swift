//
//  LiveSession.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/3/19.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import RxRelay

enum LiveType: Int {
    case singleBroadcaster = 1, multiBroadcasters, pkBroadcasters
    
    var description: String {
        switch self {
        case .multiBroadcasters:
            return NSLocalizedString("Multi_Broadcasters")
        case .singleBroadcaster:
            return NSLocalizedString("Single_Broadcaster")
        case .pkBroadcasters:
            return NSLocalizedString("PK_Live")
        }
    }
    
    static let list: [LiveType] = [.multiBroadcasters,
                                   .singleBroadcaster,
                                   .pkBroadcasters]
}

class LiveSession: NSObject {
    enum Owner {
        case localUser, otherUser(RemoteOwner)
        
        var isLocal: Bool {
            switch self {
            case .localUser: return true
            case .otherUser: return false
            }
        }
        
        var remoteUser: RemoteOwner? {
            switch self {
            case .otherUser(let user): return user
            default:                   return nil
            }
        }
    }
    
    var roomId: String
    
    var settings: LocalLiveSettings
    var type: LiveType
    var role: LiveRole?
    var owner: Owner = .localUser
    
    var rtcChannelReport: BehaviorRelay<ChannelReport>?
    var end = PublishRelay<Bool>()
    var ownerInfoUpdate = PublishRelay<RemoteOwner>()
    
    init(roomId: String, settings: LocalLiveSettings, type: LiveType) {
        self.roomId = roomId
        self.settings = settings
        self.type = type
        super.init()
        self.observe()
    }
    
    typealias JoinedInfo = (seatInfo: [StringAnyDic]?, giftAudience: [StringAnyDic]?, pkInfo: StringAnyDic?)
    
    func join(success: ((JoinedInfo) throws -> Void)? = nil, fail: Completion = nil ) {
        let client = ALCenter.shared().centerProvideRequestHelper()
        let url = URLGroup.joinLive(roomId: self.roomId)
        let event = RequestEvent(name: "live-session-join")
        let task = RequestTask(event: event,
                               type: .http(.post, url: url),
                               timeout: .low,
                               header: ["token": ALKeys.ALUserToken])
        
        let successCallback: DicEXCompletion = { [unowned self] (json) in
            let data = try json.getDataObject()
            
            // Local User
            let localUserJson = try data.getDictionaryValue(of: "user")
            try self.joinAndInitRoleWith(info: localUserJson)
            
            // Live Room
            let liveRoom = try data.getDictionaryValue(of: "room")
            try self.joinAndUpdateLiveRoomInfo(info: liveRoom)
            
            // join rtc, rtm channel
            ALKeys.AgoraRtcToken = try localUserJson.getStringValue(of: "rtcToken")
            
            let channel = try liveRoom.getStringValue(of: "channelName")
            
            let mediaKit = ALCenter.shared().centerProvideMediaHelper()
            let agoraUserId = try localUserJson.getIntValue(of: "uid")
            self.setupMediaSettings(self.settings.media)
            
            mediaKit.join(channel: channel, token: ALKeys.AgoraRtcToken, streamId: agoraUserId) { [unowned self] in
                mediaKit.addEvent(.channelStats({ (stats) in
                    if self.rtcChannelReport == nil {
                        self.rtcChannelReport = BehaviorRelay(value: stats)
                    } else {
                        self.rtcChannelReport?.accept(stats)
                    }
                }), observer: self)
            }
            
            let rtm = ALCenter.shared().centerProvideRTMHelper()
            
            // only multiBroadcasters has seatInfo
            var seatInfo: [StringAnyDic]?
            if self.type == .multiBroadcasters {
                seatInfo = try liveRoom.getListValue(of: "coVideoSeats")
            }
            
            // only pkBroadcaster has pkInfo
            var pkInfo: StringAnyDic?
            if self.type == .pkBroadcasters {
                pkInfo = try liveRoom.getDictionaryValue(of: "pk")
            }
            
            let giftAudience = try? liveRoom.getListValue(of: "rankUsers")
            
            rtm.joinChannel(channel, success: {
                guard let success = success else {
                    return
                }
                do {
                    try success((seatInfo, giftAudience, pkInfo))
                } catch {
                    if let fail = fail {
                        fail()
                    }
                }
            }) { [unowned mediaKit] (error) -> RetryOptions in
                mediaKit.leaveChannel()
                return .resign
            }
        }
        let response = AGEResponse.json(successCallback)
        
        let retry: ErrorRetryCompletion = { (error: AGEError) -> RetryOptions in
            if let fail = fail {
                fail()
            }
            return .resign
        }
        
        client.request(task: task, success: response, failRetry: retry)
    }
    
    @discardableResult func audienceToBroadcaster() -> LiveRole {
        guard let audience = self.role as? LiveAudience else {
            fatalError()
        }
        
        let media = ALCenter.shared().centerProvideMediaHelper()
        media.capture.audio = .on
        try! media.capture.video(.on)
        var status = audience.status
        status.insert(.camera)
        status.insert(.mic)
        let role = MultiBroadBroadcaster(info: audience.info,
                                         giftRank: audience.giftRank,
                                         status: status,
                                         agoraUserId: audience.agoraUserId)
        self.role = role
        self.setupMediaSettings(settings.media)
        return role
    }
    
    @discardableResult func broadcasterToAudience() -> LiveRole {
        guard let broadcaster = self.role as? MultiBroadBroadcaster else {
            fatalError()
        }
        
        let media = ALCenter.shared().centerProvideMediaHelper()
        media.capture.audio = .off
        try! media.capture.video(.off)
        let role = LiveAudience(info: broadcaster.info,
                                giftRank: broadcaster.giftRank,
                                agoraUserId: broadcaster.agoraUserId)
        
        self.role = role
        return role
    }
    
    func setupMediaSettings(_ settings: LocalLiveSettings.Media) {
        let mediaKit = ALCenter.shared().centerProvideMediaHelper()
        
        mediaKit.setupVideo(resolution: settings.resolution,
                            frameRate: settings.frameRate,
                            bitRate: settings.bitRate)
    }
    
    func leave() {
        let mediaKit = ALCenter.shared().centerProvideMediaHelper()
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        let client = ALCenter.shared().centerProvideRequestHelper()
        role = nil
        mediaKit.removeObserver(self)
        try! mediaKit.capture.video(.off)
        mediaKit.capture.audio = .off
        mediaKit.leaveChannel()
        
        rtm.leaveChannel()
        
        let event = RequestEvent(name: "live-session-leave")
        let url = URLGroup.leaveLive(roomId: self.roomId)
        let task = RequestTask(event: event, type: .http(.post, url: url))
        client.request(task: task)
    }
    
    deinit {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        rtm.removeReceivedChannelMessage(observer: self)
    }
}

private extension LiveSession {
    func joinAndInitRoleWith(info: StringAnyDic) throws {
        // Local User
        let userInfo = try BasicUserInfo(dic: info)
        let status = try UserStatus.initWith(dic: info)
        let roleType = try info.getEnum(of: "role", type: LiveRoleType.self)
        
        let giftRank = try info.getIntValue(of: "rank")
        let agoraUserId = try info.getIntValue(of: "uid")
        
        // Create Live role
        switch (self.type, roleType) {
        case (_, .owner):
            self.role = LiveOwner(info: userInfo, status: status, agoraUserId: agoraUserId)
        case (.multiBroadcasters, .broadcaster):
            self.role = MultiBroadBroadcaster(info: userInfo, giftRank: giftRank, status: status, agoraUserId: agoraUserId)
        case (_, .audience):
            self.role = LiveAudience(info: userInfo, giftRank: giftRank, agoraUserId: agoraUserId)
        default:
            fatalError()
        }
    }
    
    func joinAndUpdateLiveRoomInfo(info: StringAnyDic) throws {
        // Live room owner
        var ownerJson = try info.getDictionaryValue(of: "owner")
        ownerJson["avatar"] = "Fake"
        let ownerObj = try RemoteOwner(dic: ownerJson)
        
        guard let current = ALCenter.shared().current else {
            fatalError()
        }
        
        if ownerObj.info.userId == current.publicInfo.value.userId {
            self.owner = .localUser
        } else {
            self.owner = .otherUser(ownerObj)
        }
        
        // Live type check
        let liveType = try info.getEnum(of: "type", type: LiveType.self)
        
        guard self.type == liveType else {
            throw AGEError.fail("local live type is not equal to server live type")
        }
    }
}

private extension LiveSession {
    func observe() {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        rtm.addReceivedChannelMessage(observer: self) { [weak self] (json) in
            guard let cmd = try? json.getEnum(of: "cmd", type: ALChannelMessage.AType.self) else {
                return
            }
            
            guard let strongSelf = self else {
                return
            }
            
            switch cmd {
            case .liveEnd:
                strongSelf.end.accept(true)
            case .owner:
                let data = try json.getDataObject()
                let owner = try RemoteOwner(dic: data)
                
                if !strongSelf.owner.isLocal {
                    strongSelf.owner = .otherUser(owner)
                }
                
                strongSelf.ownerInfoUpdate.accept(owner)
            default:
                break
            }
        }
    }
}
