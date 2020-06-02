//
//  VirtualVM.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/6/1.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit

class VirtualVM: NSObject {
    func invite() {
        
    }
    
    deinit {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        rtm.removeReceivedChannelMessage(observer: self)
    }
    
    func inviteAudioToBroadcasting() {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
//        ALPeerMessage
//        let message = ALPeerMessage(type: .broadcasting,
//                                    command: .inviteBroadcasting(seatIndex: seat.index),
//                                    userName: local.info.name,
//                                    userId: local.info.userId,
//                                    agoraUid: local.agoraUserId)
//        
//        do {
//            let jsonString = try message.json().jsonString()
//            try rtm.write(message: jsonString,
//                          of: RequestEvent(name: "apply_for_broadcasting"),
//                          to: "\(user.agoraUserId)",
//                          fail: fail)
//        } catch let error as AGEError {
//            if let fail = fail {
//                fail(error)
//            }
//        } catch {
//            if let fail = fail {
//                fail(AGEError.unknown())
//            }
//        }
    }
}

private extension VirtualVM {
    func observe() {
        let rtm = ALCenter.shared().centerProvideRTMHelper()
        rtm.addReceivedChannelMessage(observer: self) { [weak self] (json) in
            guard let cmd = try? json.getEnum(of: "cmd", type: ALChannelMessage.AType.self) else {
                return
            }
            
            guard cmd == .seats else {
                return
            }
            
//            let list = try json.getListValue(of: "data")
//            var tempList = [LiveSeat]()
//            for item in list {
//                let seat = try LiveSeat(dic: item)
//                tempList.append(seat)
//            }
//            self?.list.accept(tempList.sorted(by: {$0.index < $1.index}))
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
            
//            switch cmd {
//            case  101: //.applyForBroadcasting:
//                let index = try data.getIntValue(of: "coindex")
//                self?.receivedBroadcasting.accept((userName, userId, agoraUid, index))
//            case  102: // .inviteBroadcasting:
//                let index = try data.getIntValue(of: "coindex")
//                self?.receivedInvite.accept((userName, userId, agoraUid, index))
//            case  103: // .rejectBroadcasting:
//                self?.receivedRejectBroadcasting.accept(userName)
//            case  104: // .rejectInviteBroadcasting:
//                self?.receivedRejectInviteBroadcasting.accept(userName)
//            case  105: // .acceptBroadcastingRequest:
//                break
//            case  106: // .acceptInvitingRequest:
//                break
//            case  201: // .invitePK:
//                break
//            case  202: // .rejectPK:
//                break
//            default:
//                #if DEBUG
//                fatalError("error cmd: \(cmd)")
//                #else
//                break
//                #endif
//            }
        }
    }
}
