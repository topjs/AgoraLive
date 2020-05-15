//
//  ALCenter.swift
//  Center
//
//  Created by CavanSu on 2020/2/11.
//  Copyright © 2020 CavanSu. All rights reserved.
//

import UIKit
import RxSwift
import RxRelay

class ALCenter: NSObject {
    static let instance = ALCenter()
    static func shared() -> ALCenter {
        return instance
    }
    
    var isWorkNormally = BehaviorRelay(value: false)
    
    // Abstractions
    private var files = FilesGroup()
    
    private(set) var current: CurrentUser?
    
    lazy var appAssistant = AppAssistant()
    var liveSession: LiveSession?
    
    // Commons
    private let log = LogTube()
    private lazy var mediaKit = MediaKit(log: log)
    
    private lazy var alamo = AlamoClient(logTube: log,
                                         delegate: nil)
    
    private lazy var rtm = RTMClient(logTube: log)
    private lazy var userDataHelper = UserDataHelper()
}

extension ALCenter {
    func registerAndLogin() {
        if let current = CurrentUser.local() {
            self.current = current
            self.login(userId: current.info.userId) {
                self.isWorkNormally.accept(true)
            }
            return
        }
        
        register { [unowned self] (info: BasicUserInfo) in
            self.login(userId: info.userId) { [unowned self] in
                self.current = CurrentUser(info: info)
                self.isWorkNormally.accept(true)
            }
        }
    }
    
    func createLiveSession(roomSettings: LocalLiveSettings, type: LiveType, success: ((LiveSession) -> Void)? = nil, fail: Completion = nil) {
        let url = URLGroup.liveCreate
        let event = RequestEvent(name: "live-session-create")
        let parameter: StringAnyDic = ["roomName": roomSettings.title, "type": type.rawValue]
        let task = RequestTask(event: event,
                               type: .http(.post, url: url),
                               timeout: .medium,
                               header: ["token": ALKeys.ALUserToken],
                               parameters: parameter)
        
        let successCallback: DicEXCompletion = { (json: ([String: Any])) throws in
            let roomId = try json.getStringValue(of: "data")
            
            let session = LiveSession(roomId: roomId, settings: roomSettings, type: type)
            
            if let success = success {
                success(session)
            }
        }
        let response = AGEResponse.json(successCallback)
        
        let retry: ErrorRetryCompletion = { (error: AGEError) -> RetryOptions in
            if let fail = fail {
                fail()
            }
            return .resign
        }
        
        alamo.request(task: task, success: response, failRetry: retry)
    }
    
    func reinitAgoraServe() {
        mediaKit.reinitRTC()
        rtm = RTMClient(logTube: log)
    }
}

private extension ALCenter {
    func register(success: ((BasicUserInfo) -> Void)?) {
        let url = URLGroup.userRegister
        let event = RequestEvent(name: "user-register")
        let name = "Uknow"
        let parameters = ["userName": name]
        let task = RequestTask(event: event,
                               type: .http(.post, url: url),
                               timeout: .low,
                               parameters: parameters)
        
        let successCallback: DicEXCompletion = { (json: ([String: Any])) throws in
            let object = try json.getDataObject()
            let userId = try object.getStringValue(of: "userId")
            let info = BasicUserInfo(userId: userId,
                                     name: name,
                                     headURL: "local")
            
            if let success = success {
                success(info)
            }
        }
        let response = AGEResponse.json(successCallback)
        
        let retry: ErrorRetryCompletion = { (error: AGEError) -> RetryOptions in
            return .retry(after: 1, newTask: nil)
        }
        
        alamo.request(task: task, success: response, failRetry: retry)
    }
    
    func login(userId: String, success: Completion) {
        let url = URLGroup.userLogin
        let event = RequestEvent(name: "user-login")
        let task = RequestTask(event: event,
                               type: .http(.post, url: url),
                               timeout: .low,
                               parameters: ["userId": userId])
        
        let successCallback: DicEXCompletion = { (json: ([String: Any])) throws in
            let object = try json.getDataObject()
            let userToken = try object.getStringValue(of: "userToken")
            let rtmToken = try object.getStringValue(of: "rtmToken")
            let uid = try object.getIntValue(of: "uid")
            ALKeys.ALUserToken = userToken
            ALKeys.AgoraRtmToken = rtmToken
            
            // RTM Login
            try? self.rtm.connect(rtmId: "\(uid)", token: rtmToken, success: {
                if let success = success {
                    success()
                }
            }) { (error) -> RetryOptions in
                return .retry(after: 0.5, newTask: nil)
            }
        }
        let response = AGEResponse.json(successCallback)
        
        let retry: ErrorRetryCompletion = { (error: AGEError) -> RetryOptions in
            return .retry(after: 1, newTask: nil)
        }
        
        alamo.request(task: task, success: response, failRetry: retry)
    }
}

extension ALCenter: CenterHelper {
    func centerProvideRequestHelper() -> AlamoClient {
        return alamo
    }
    
    func centerProvideImagesHelper() -> ImageFiles {
        return files.images
    }
    
    func centerProvideMediaHelper() -> MediaKit {
        return mediaKit
    }
    
    func centerProvideFilesGroup() -> FilesGroup {
        return self.files
    }
    
    func centerProvideRTMHelper() -> RTMClient {
        return self.rtm
    }
    
    func centerProvideLogTubeHelper() -> LogTube {
        return self.log
    }
    
    func centerProvideUserDataHelper() -> UserDataHelper {
        return userDataHelper
    }
}
