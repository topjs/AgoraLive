//
//  RTMProtocols.swift
//  AGECenter
//
//  Created by CavanSu on 2019/6/23.
//  Copyright © 2019 Agora. All rights reserved.
//

import Foundation

protocol SocketProtocol {
    var connectStatus: AGESocketState {set get}
        
    func connect(rtmId: String, token: String?, success: Completion, failRetry: ErrorRetryCompletion) throws
    func disconnect()
    func write(message: String, of event: AGERequestEvent, to: String, success: Completion, fail: ErrorCompletion) throws
    func renew(token: String, fail: ErrorCompletion)
    
    func addReceivedPeerMessage(observer: NSObject, subscribe: DicEXCompletion)
    func removeReceivedPeerMessage(observer: NSObject)
    
    func addReceivedChannelMessage(observer: NSObject, subscribe: DicEXCompletion)
    func removeReceivedChannelMessage(observer: NSObject)
    
    func addConnectStatusChnage(observer: NSObject, subscribe: ((AGESocketState) -> Void)?)
    func removeConnectStatusChnage(observer: NSObject)
    
    func addOccurError(observer: NSObject, subscribe: ErrorCompletion)
    func removeOccurError(observer: NSObject)
}

struct ChannelUser {
    enum State {
        case enter, left
    }

    var uid: String
    var state: State
}

protocol RTMChannelProtocol {
    func joinChannel(_ channel: String, success: Completion, failRetry: ErrorRetryCompletion)
    func leaveChannel()
    func writeChannel(message: String, of event: AGERequestEvent, success: Completion, fail: ErrorCompletion) throws
    
    func addUserOfChannel(observer: NSObject, subscribe: ((ChannelUser) -> Void)?)
    func removeUserOfChannel(observer: NSObject)
}
