//
//  LiveSeatVM.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/3/26.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import RxRelay

enum SeatState: Int {
    case empty = 0, normal, close
}

enum SeatCommand {
    // 禁麦， 解禁， 封麦，下麦， 解封， 邀请，
    case ban, unban, close, forceToAudience, release, invite
    // 申请成为主播， 主播下麦
    case applyForBroadcasting, endBroadcasting
    
    case none
    
    var description: String {
        switch self {
        case .ban:                  return NSLocalizedString("Seat_Ban")
        case .unban:                return NSLocalizedString("Seat_Unban")
        case .forceToAudience:      return NSLocalizedString("End_Broadcasting")
        case .close:                return NSLocalizedString("Seat_Close")
        case .release:              return NSLocalizedString("Seat_Release")
        case .invite:               return "invite"
        case .applyForBroadcasting: return NSLocalizedString("Apply_For_Broadcasting")
        case .endBroadcasting:      return NSLocalizedString("End_Broadcasting")
        case .none:                 return "none"
        }
    }
}

struct LiveSeat {
    var user: RemoteBroadcaster?
    var index: Int // 1 ... 6
    var state: SeatState
    
    init(dic: StringAnyDic) throws {
        let seatJson = try dic.getDictionaryValue(of: "seat")
        self.index = try seatJson.getIntValue(of: "no")
        self.state = try seatJson.getEnum(of: "state", type: SeatState.self)
        
        if self.state == .normal {
            let broadcaster = try dic.getDictionaryValue(of: "user")
            self.user = try RemoteBroadcaster(dic: broadcaster)
        }
    }
}

class LiveSeatVM: NSObject {
    var list: BehaviorRelay<[LiveSeat]>
    
    typealias PeerInfo = (userName: String, userId: String, agoraUserId: Int, seatIndex: Int?)
    
    // Owner
    var receivedBroadcasting = PublishRelay<PeerInfo>() // String: remote user name, String: remote agoraUid, Int: Seat Index
    var receivedRejectInviteBroadcasting = PublishRelay<String>() // String: remote user name
    
    // Audience
    var receivedInvite = PublishRelay<PeerInfo>() // String: remote user name, String: remote agoraUid, Int: seat index
    var receivedRejectBroadcasting = PublishRelay<String>()
    var receivedForceEndBroadcasting = PublishRelay<PeerInfo>()
    
    init(list: [StringAnyDic]) throws {
        var tempList = [LiveSeat]()
        
        for item in list {
            let seat = try LiveSeat(dic: item)
            tempList.append(seat)
        }
        
        self.list = BehaviorRelay(value: tempList.sorted(by: {$0.index < $1.index}))
        
        super.init()
        observe()
    }
    
    deinit {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        rtm.removeReceivedChannelMessage(observer: self)
    }
}

private extension LiveSeatVM {
    func observe() {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        rtm.addReceivedChannelMessage(observer: self) { [weak self] (json) in
            guard let cmd = try? json.getEnum(of: "cmd", type: ALChannelMessage.AType.self) else {
                return
            }
            
            guard cmd == .seats else {
                return
            }
            
            let list = try json.getListValue(of: "data")
            var tempList = [LiveSeat]()
            for item in list {
                let seat = try LiveSeat(dic: item)
                tempList.append(seat)
            }
            self?.list.accept(tempList.sorted(by: {$0.index < $1.index}))
        }
        
        rtm.addReceivedPeerMessage(observer: self) { [weak self] (json) in
            guard let type = try? json.getEnum(of: "cmd", type: ALPeerMessage.AType.self),
                type == .broadcasting else {
                return
            }
            
            let data = try json.getDataObject()
            let cmd = try data.getIntValue(of: "operate")
            let userName = try data.getStringValue(of: "account")
            let userId = try data.getStringValue(of: "userId")
            let agoraUid = try data.getIntValue(of: "agoraUid")
            
            switch cmd {
            case  101: //.applyForBroadcasting:
                let index = try data.getIntValue(of: "coindex")
                self?.receivedBroadcasting.accept((userName, userId, agoraUid, index))
            case  102: // .inviteBroadcasting:
                let index = try data.getIntValue(of: "coindex")
                self?.receivedInvite.accept((userName, userId, agoraUid, index))
            case  103: // .rejectBroadcasting:
                self?.receivedRejectBroadcasting.accept(userName)
            case  104: // .rejectInviteBroadcasting:
                self?.receivedRejectInviteBroadcasting.accept(userName)
            case  105: // .acceptBroadcastingRequest:
                break
            case  106: // .acceptInvitingRequest:
                break
            case  201: // .invitePK:
                break
            case  202: // .rejectPK:
                break
            default:
                #if DEBUG
                fatalError("error cmd: \(cmd)")
                #else
                break
                #endif
            }
        }
    }
}

// MARK: - Owner
extension LiveSeatVM {
    func localOwner(_ local: LiveOwner, command: SeatCommand, on seat:LiveSeat, with audience: RemoteAudience? = nil,
                  of roomId: String, success: Completion = nil, fail: ErrorCompletion = nil) {
        // 禁麦， 封麦，下麦， 解封， 邀请，
        // case ban, close, forceToAudience, release, invite, none
        
        var url: String
        var parameters: StringAnyDic
        
        switch command {
        // seat status
        case .invite:
            guard let user = audience else {
                fatalError()
            }
            
            let rtm = ALCenter.shared().centerProvideRTMHelper()
            let message = ALPeerMessage(type: .broadcasting,
                                        command: .inviteBroadcasting(seatIndex: seat.index),
                                        userName: local.info.name,
                                        userId: local.info.userId,
                                        agoraUid: local.agoraUserId)
            
            do {
                let jsonString = try message.json().jsonString()
                try rtm.write(message: jsonString,
                              of: RequestEvent(name: "apply_for_broadcasting"),
                              to: "\(user.agoraUserId)",
                              fail: fail)
            } catch let error as AGEError {
                if let fail = fail {
                    fail(error)
                }
            } catch {
                if let fail = fail {
                    fail(AGEError.unknown())
                }
            }
            return
        case .close:
            url = URLGroup.liveSeatCommand(roomId: roomId)
            parameters = ["no": seat.index,
                          "userId": 0,
                          "state": 2]
        case .forceToAudience:
            url = URLGroup.liveSeatCommand(roomId: roomId)
            parameters = ["no": seat.index,
                          "userId": 0,
                          "state": 0]
        case .release:
            url = URLGroup.liveSeatCommand(roomId: roomId)
            parameters = ["no": seat.index,
                          "userId": 0,
                          "state": 0]
            
        // user status
        case .ban, .unban:
            guard let user = seat.user else {
                fatalError()
            }
            url = URLGroup.userCommand(userId: user.info.userId, roomId: roomId)
            parameters = ["enableAudio": command == .ban ? 0 : 1,
                          "enableVideo": user.status.contains(.camera) ? 1 : 0,
                          "enableChat": user.status.contains(.chat) ? 1 : 0]
        default:
            return
        }
        
        let client = ALCenter.shared().centerProvideRequestHelper()
        let event = RequestEvent(name: "live-seat-command: \(command.description)")
        
        let token = ["token": ALKeys.ALUserToken]
        let task = RequestTask(event: event,
                               type: .http(.post, url: url),
                               header: token,
                               parameters: parameters)
        let successCallback: DicEXCompletion = { (json) in
            try json.getCodeCheck()
            let isSuccess = try json.getBoolInfoValue(of: "data")
            if isSuccess, let callback = success {
                callback()
            } else if !isSuccess, let callback = fail {
                callback(AGEError.fail("live-seat-command fail") )
            }
        }
        let response = AGEResponse.json(successCallback)
        
        let fail: ErrorRetryCompletion = { (error) in
            if let callback = fail {
                callback(error)
            }
            return .resign
        }
        
        client.request(task: task, success: response, failRetry: fail)
    }
    
    func localOwnerAcceptBroadcasting(audience userId: String, seatIndex: Int, roomId: String, success: Completion = nil, fail: ErrorCompletion = nil) {
        let url = URLGroup.liveSeatCommand(roomId: roomId)
        let parameters: StringAnyDic = ["no": seatIndex,
                                        "userId": userId,
                                        "state": 1]
        
        let client = ALCenter.shared().centerProvideRequestHelper()
        let event = RequestEvent(name: "live-seat-command: accept broadcasting")
        
        let token = ["token": ALKeys.ALUserToken]
        let task = RequestTask(event: event,
                               type: .http(.post, url: url),
                               header: token,
                               parameters: parameters)
        let successCallback: DicEXCompletion = { (json) in
            try json.getCodeCheck()
            let isSuccess = try json.getBoolInfoValue(of: "data")
            if isSuccess, let callback = success {
                callback()
            } else if !isSuccess, let callback = fail {
                callback(AGEError.fail("live-seat-command fail") )
            }
        }
        let response = AGEResponse.json(successCallback)
        
        let fail: ErrorRetryCompletion = { (error) in
            if let callback = fail {
                callback(error)
            }
            return .resign
        }
        
        client.request(task: task, success: response, failRetry: fail)
    }
    
    func localOwner(_ local: LiveOwner, rejectBroadcastingAudience agoraUid: Int, fail: ErrorCompletion = nil) {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        let message = ALPeerMessage(type: .broadcasting,
                                    command: .rejectBroadcasting,
                                    userName: local.info.name,
                                    userId: local.info.userId,
                                    agoraUid: local.agoraUserId)
        
        do {
            let jsonString = try message.json().jsonString()
            try rtm.write(message: jsonString,
                          of: RequestEvent(name: "apply_for_broadcasting"),
                          to: "\(agoraUid)",
                          fail: fail)
        } catch let error as AGEError {
            if let fail = fail {
                fail(error)
            }
        } catch {
            if let fail = fail {
                fail(AGEError.unknown())
            }
        }
    }
}

// MARK: - LiveBroadcaster
extension LiveSeatVM {
    func localBroadcaster(_ local: MultiBroadBroadcaster, endBroadcastingOn seat:LiveSeat, of roomId: String, success: Completion = nil, fail: Completion = nil) {
        let url = URLGroup.liveSeatCommand(roomId: roomId)
        let parameters: StringAnyDic = ["no": seat.index,
                                        "userId": local.info.userId,
                                        "state": 0]
        let client = ALCenter.shared().centerProvideRequestHelper()
        let event = RequestEvent(name: "live-seat-broadcaste-end")
        
        let token = ["token": ALKeys.ALUserToken]
        let task = RequestTask(event: event,
                               type: .http(.post, url: url),
                               header: token,
                               parameters: parameters)
        let successCallback: DicEXCompletion = { (json) in
            try json.getCodeCheck()
            let isSuccess = try json.getBoolInfoValue(of: "data")
            if isSuccess, let callback = success {
                callback()
            } else if !isSuccess, let callback = fail {
                callback()
            }
        }
        let response = AGEResponse.json(successCallback)
        
        let fail: ErrorRetryCompletion = { (error) in
            if let callback = fail {
                callback()
            }
            return .resign
        }
        
        client.request(task: task, success: response, failRetry: fail)
    }
}

// MARK: - Audience
extension LiveSeatVM {
    func localAudience(_ local: LiveAudience, applyForBroadcastingToOwner agoraUid: Int, seat: LiveSeat, fail: ErrorCompletion) {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        let message = ALPeerMessage(type: .broadcasting,
                                    command: .applyForBroadcasting(seatIndex: seat.index),
                                    userName: local.info.name,
                                    userId: local.info.userId,
                                    agoraUid: local.agoraUserId)
        
        do {
            let jsonString = try message.json().jsonString()
            try rtm.write(message: jsonString,
                          of: RequestEvent(name: "apply_for_broadcasting"),
                          to: "\(agoraUid)",
                          fail: fail)
        } catch let error as AGEError {
            if let fail = fail {
                fail(error)
            }
        } catch {
            if let fail = fail {
                fail(AGEError.unknown())
            }
        }
    }
    
    func localAudience(_ local: LiveAudience, acceptInvitingOn seatIndex: Int, roomId: String, success: Completion = nil, fail: ErrorCompletion = nil) {
        let url = URLGroup.liveSeatCommand(roomId: roomId)
        let parameters: StringAnyDic = ["no": seatIndex,
                                        "userId": local.info.userId,
                                        "state": 1]
        
        let client = ALCenter.shared().centerProvideRequestHelper()
        let event = RequestEvent(name: "live-seat-command: accpe")
        
        let token = ["token": ALKeys.ALUserToken]
        let task = RequestTask(event: event,
                               type: .http(.post, url: url),
                               header: token,
                               parameters: parameters)
        let successCallback: DicEXCompletion = { (json) in
            try json.getCodeCheck()
            let isSuccess = try json.getBoolInfoValue(of: "data")
            if isSuccess, let callback = success {
                callback()
            } else if !isSuccess, let callback = fail {
                callback(AGEError.fail("live-seat-command fail") )
            }
        }
        let response = AGEResponse.json(successCallback)
        
        let fail: ErrorRetryCompletion = { (error) in
            if let callback = fail {
                callback(error)
            }
            return .resign
        }
        
        client.request(task: task, success: response, failRetry: fail)
    }
    
    func localAudience(_ local: LiveAudience, rejectInvitingFrom ownerId: Int, fail: ErrorCompletion = nil) {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        let message = ALPeerMessage(type: .broadcasting,
                                    command: .rejectInviteBroadcasting,
                                    userName: local.info.name,
                                    userId: local.info.userId,
                                    agoraUid: local.agoraUserId)
        
        do {
            let jsonString = try message.json().jsonString()
            try rtm.write(message: jsonString,
                          of: RequestEvent(name: "apply_for_broadcasting"),
                          to: "\(ownerId)",
                          fail: fail)
        } catch let error as AGEError {
            if let fail = fail {
                fail(error)
            }
        } catch {
            if let fail = fail {
                fail(AGEError.unknown())
            }
        }
    }
}
