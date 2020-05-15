//
//  MediaSubs.swift
//  AGECenter
//
//  Created by CavanSu on 2019/7/9.
//  Copyright © 2019 Agora. All rights reserved.
//

import AgoraRtcKit
import Foundation

class Capture: NSObject {
    private var agoraKit: AgoraRtcEngineKit {
        return MediaKit.rtcKit
    }
    
    private var cameraSession: AGESingleCamera?
    
    private(set) var video: AGESwitch = .off {
        didSet {
            guard oldValue != video else {
                return
            }
            
            parent.checkChannelProfile()
            
            #if (!arch(i386) && !arch(x86_64))
            if parent.channelStatus == .out {
                switch video {
                case .on:  agoraKit.startPreview()
                case .off: agoraKit.stopPreview()
                }
            }
            agoraKit.enableLocalVideo(video.boolValue)
            #endif
        }
    }
    
    var audio: AGESwitch = .off {
        didSet {
            guard oldValue != audio else {
                return
            }
            agoraKit.enableLocalAudio(audio.boolValue)
            parent.checkChannelProfile()
        }
    }
    
    func video(_ action: AGESwitch) throws {
        if cameraSession == nil {
            #if (!arch(i386) && !arch(x86_64))
            cameraSession = try AGESingleCamera(position: cameraPostion)
            cameraSession?.delegate = parent
            #endif
        }
        
        switch action {
        case .on:
            #if (!arch(i386) && !arch(x86_64))
            agoraKit.setVideoSource(parent)
            #endif
            video = .on
            #if (!arch(i386) && !arch(x86_64))
            try cameraSession?.start(work: .capture)
            #endif
        case .off:
            #if (!arch(i386) && !arch(x86_64))
            cameraSession?.stopWork()
            #endif
            video = .off
            #if (!arch(i386) && !arch(x86_64))
            agoraKit.setVideoSource(nil)
            #endif
        }
    }
    
    #if os(iOS)
    var cameraPostion: Position = .front {
        didSet {
            guard cameraPostion != oldValue else {
                return
            }
            try? cameraSession?.switchPosition(cameraPostion)
        }
    }
    #endif
    
    private var parent: MediaKit
    
    init(parent: MediaKit) {
        self.parent = parent
    }
    
    #if os(iOS)
    func switchCamera() throws {
        guard let camera = cameraSession else {
            throw AGEError(type: .valueNil("camera session"))
        }
         
        try camera.switchPosition(camera.position.toggle)
    }
    #endif
}

class Player: NSObject, AGELogBase {
    var logTube: LogTube {
        get {
            return ALCenter.shared().centerProvideLogTubeHelper()
        }
        set {
        }
    }
    
    enum Event {
        case audioOutputRouting(((AudioOutputRouting) -> Void)? = nil)
    }
    
    typealias Priority = AgoraUserPriority
    typealias RenderMode = AgoraVideoRenderMode
    typealias VideoStreamType = AgoraVideoStreamType
    
    private lazy var observers = [NSObject: Event]()
    private(set) var isLocalAudioLoop = false
    
    var audioRoute: AudioOutputRouting = .default {
        didSet {
            for (_, event) in observers {
                switch event {
                case .audioOutputRouting(let callback):
                    if let tCallback = callback {
                        tCallback(audioRoute)
                    }
                }
            }
        }
    }
    
    var mixFileAudioFinish: (() -> Void)? = nil
    
    private var agoraKit: AgoraRtcEngineKit {
        return MediaKit.rtcKit
    }
    
    func renderLocalVideoStream(id: Int, view: UIView) {
        log(info: "render local video stream", extra: "id: \(id), view frame: \(view.frame)")
        let canvas = AgoraRtcVideoCanvas(streamId: id, view: view)
        agoraKit.setupLocalVideo(canvas)
    }
    
    func renderRemoteVideoStream(id: Int, view: UIView) {
        log(info: "render remote video stream", extra: "id: \(id), view frame: \(view.frame)")
        let canvas = AgoraRtcVideoCanvas(streamId: id, view: view)
        agoraKit.setupRemoteVideo(canvas)
    }
    
    func renderRemoteVideoStream(id: Int, superResolution action: AGESwitch) {
        agoraKit.enableRemoteSuperResolution(UInt(id), enabled: action.boolValue)
    }
    
    func setRemoteVideoStream(id: Int, type: VideoStreamType) {
        agoraKit.setRemoteVideoStream(UInt(id), type: type)
    }
    
    func setRemoteVideoStream(id: Int, renderMode: RenderMode) {
        agoraKit.setRemoteRenderMode(UInt(id), mode: renderMode)
    }
    
    func render(priority: Priority, remoteVideoStream id: Int) {
        agoraKit.setRemoteUserPriority(UInt(id), type: priority)
    }
    
    func render(_ action: AGESwitch, remoteVideoStream id: Int) {
        agoraKit.muteRemoteVideoStream(UInt(id), mute: action.boolValue)
    }
    
    func startMixingFileAudio(url: String, finish: (() -> Void)? = nil) {
        self.mixFileAudioFinish = finish
        agoraKit.startAudioMixing(url, loopback: true, replace: false, cycle: 1)
    }
    
    func pauseMixFileAudio() {
        agoraKit.pauseAudioMixing()
    }
    
    func resumeMixFileAudio() {
        agoraKit.resumeAudioMixing()
    }
    
    func localInputAudioLoop(_ action: AGESwitch) {
        agoraKit.enable(inEarMonitoring: action.boolValue)
        isLocalAudioLoop = action.boolValue
    }
    
    func addEvent(_ event: Event, observer: NSObject) {
        self.observers[observer] = event
    }
    
    func removeObserver(_ observer: NSObject) {
        self.observers.removeValue(forKey: observer)
    }
}

// MARK: Log
private extension Player {
    func log(info: String, extra: String? = nil, funcName: String = #function) {
        let className = Player.self
        logOutputInfo(info, extra: extra, className: className, funcName: funcName)
    }
    
    func log(warning: String, extra: String? = nil, funcName: String = #function) {
        let className = Player.self
        logOutputWarning(warning, extra: extra, className: className, funcName: funcName)
    }
    
    func log(error: Error, extra: String? = nil, funcName: String = #function) {
        let className = Player.self
        logOutputError(error, extra: extra, className: className, funcName: funcName)
    }
}

class VideoEnhancement: NSObject {
    var work: AGESwitch = .off
    
    override init() {
        super.init()
        FUManager.share()?.loadItems()
    }
    
    var lightening: Double {
        set {
            FUManager.share()?.eyelightingLevel = newValue
        }
        
        get {
            return FUManager.share()?.eyelightingLevel ?? 0
        }
    }
    
    var redness: Double {
        set {
            FUManager.share()?.redLevel = newValue
        }
        
        get {
            return FUManager.share()?.redLevel ?? 0
        }
    }
    
    var blurLevel: Double {
        set {
            FUManager.share()?.blurLevel = newValue
        }
        
        get {
            return FUManager.share()?.blurLevel ?? 0
        }
    }
    
    var colorLevel: Double {
        set {
            FUManager.share()?.whiteLevel = newValue
        }
        
        get {
            return FUManager.share()?.whiteLevel ?? 0
        }
    }
    
    var cheekThining: Double {
        set {
            FUManager.share()?.thinningLevel = newValue
        }
        
        get {
            return FUManager.share()?.thinningLevel ?? 0
        }
    }
    
    var eyeEnlarging: Double {
        set {
            FUManager.share()?.enlargingLevel = newValue
        }
        
        get {
            return FUManager.share()?.enlargingLevel ?? 0
        }
    }
}
