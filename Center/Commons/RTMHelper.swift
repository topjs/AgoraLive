//
//  RTMHelper.swift
//  AGEManAGEr
//
//  Created by CavanSu on 2019/4/23.
//  Copyright © 2019 Agora. All rights reserved.
//

import Foundation

enum RTMmessageType: AGEDescription {
    case peer(Int), channel
    
    var description: String {
        return cusDescription()
    }
    
    var debugDescription: String {
        return cusDescription()
    }
    
    func cusDescription() -> String {
        switch self {
        case .peer(let agoraUid):  return "peer: \(agoraUid)"
        case .channel:             return "channel"
        }
    }
}

struct RTMQueueItem {
    var event: AGERequestEvent
    var success: AGEResponse?
    var fail: ErrorCompletion
    
    init(event: AGERequestEvent, success: AGEResponse?, fail: ErrorCompletion) {
        self.event = event
        self.success = success
        self.fail = fail
    }
}

class RTMRequestQueue: NSObject {
    lazy var all = [Int: RTMQueueItem]()
    
    func push(_ item: RTMQueueItem, id: Int) {
        all[id] = item
    }
    
    @discardableResult func pop(id: Int) -> RTMQueueItem? {
        defer {
            all.removeValue(forKey: id)
        }
        return all[id]
    }
}
