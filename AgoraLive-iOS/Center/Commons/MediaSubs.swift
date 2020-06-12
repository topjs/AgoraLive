//
//  MediaSubs.swift
//  AGECenter
//
//  Created by CavanSu on 2019/7/9.
//  Copyright © 2019 Agora. All rights reserved.
//

import AgoraRtcKit
import Foundation
import AGECamera

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
            try cameraSession?.start(work: .capture(isMirror: true))
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
    var cameraPostion: AGECamera.Position {
        get {
        
            return cameraSession?.position ?? .front
        }
        
        set {
            try? cameraSession?.switchPosition(newValue)
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
    
    func renderLocalVideoStream(id: Int, view: UIView, isMirror: Bool = false) {
        log(info: "render local video stream", extra: "id: \(id), view frame: \(view.frame)")
        let canvas = AgoraRtcVideoCanvas(streamId: id, view: view)
        agoraKit.setupLocalVideo(canvas)
        agoraKit.setLocalVideoMirrorMode(isMirror ? .enabled : .disabled)
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
        agoraKit.startAudioMixing(url, loopback: false, replace: false, cycle: 1)
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

enum VirtualAppearance {
    case none, girl, dog
    
    var image: UIImage {
        get {
            switch self {
            case .girl: return UIImage(named: "portrait-girl")!
            case .dog:  return UIImage(named: "portrait-dog")!
            case .none: fatalError()
            }
        }
    }
    
    var item: String {
        get {
            switch self {
            case .girl: return "qgirl_Animoji"
            case .dog:  return "hashiqi_Animoji"
            case .none: return "noitem"
            }
        }
    }
    
    static func item(_ item: String) -> VirtualAppearance {
        switch item {
        case "qgirl_Animoji":   return VirtualAppearance.girl
        case "hashiqi_Animoji": return VirtualAppearance.dog
        default:                return VirtualAppearance.none
        }
    }
}

class VideoEnhancement: NSObject {
    var beauty: AGESwitch = .off {
        didSet {
            switch beauty {
            case .off: FUManager.share()?.destoryItems()
            case .on:  FUManager.share()?.loadItems()
            }
        }
    }
    
    static func getFUSkinParam(with tag: String) -> FUBeautyParam? {
        var param: FUBeautyParam?
        guard let array = FUManager.share()?.skinParams else {
            return nil
        }
        
        for item in array {
            let i = item as! FUBeautyParam
            if i.mParam == tag {
                param = i
                break
            }
        }
        
        return param
    }
    
    static func getFUShapeParam(with tag: String) -> FUBeautyParam? {
        var param: FUBeautyParam?
        guard let array = FUManager.share()?.shapeParams else {
            return nil
        }
        
        for item in array {
            let i = item as! FUBeautyParam
            if i.mParam == tag {
                param = i
                break
            }
        }
        
        return param
    }
    
    var blurLevel: Double {
        set {
            guard let param = VideoEnhancement.getFUSkinParam(with: "blur_level") else {
                return
            }
            
            param.mValue = Float(newValue)
            
            FUManager.share()?.setParamItemAboutType(.typeBeauty, name: param.mParam, value: param.mValue * 6)
        }
        
        get {
            guard let param = VideoEnhancement.getFUSkinParam(with: "blur_level") else {
                return 0
            }
            return Double(param.mValue)
        }
    }
    
    var colorLevel: Double {
        set {
            guard let param = VideoEnhancement.getFUSkinParam(with: "color_level") else {
                return
            }
            
            param.mValue = Float(newValue)
            
            FUManager.share()?.setParamItemAboutType(.typeBeauty, name: param.mParam, value: param.mValue)
        }
        
        get {
            guard let param = VideoEnhancement.getFUSkinParam(with: "color_level") else {
                return 0
            }
            return Double(param.mValue)
        }
    }
    
    var cheekThining: Double {
        set {
            guard let param = VideoEnhancement.getFUShapeParam(with: "cheek_thinning") else {
                return
            }
            
            param.mValue = Float(newValue)
            
            FUManager.share()?.setParamItemAboutType(.typeBeauty, name: param.mParam, value: param.mValue)
        }
        
        get {
            guard let param = VideoEnhancement.getFUShapeParam(with: "cheek_thinning") else {
                return 0
                
            }
            return Double(param.mValue)
        }
    }
    
    var eyeEnlarging: Double {
        set {
            guard let param = VideoEnhancement.getFUShapeParam(with: "eye_enlarging") else {
                return
            }
            
            param.mValue = Float(newValue)
            
            FUManager.share()?.setParamItemAboutType(.typeBeauty, name: param.mParam, value: param.mValue)
        }
        
        get {
            guard let param = VideoEnhancement.getFUShapeParam(with: "eye_enlarging") else {
                return 0
                
            }
            return Double(param.mValue)
        }
    }
    
    var virtualAppearance: VirtualAppearance = .none {
        didSet {
            switch virtualAppearance {
            case .none:
                FUManager.share()?.loadItem(virtualAppearance.item, completion: nil);
                FUManager.share()?.destoryItems()
            case .dog:
                FUManager.share()?.loadBackground()
                FUManager.share()?.loadItem(virtualAppearance.item, completion: nil);
            case .girl:
                FUManager.share()?.loadBackground()
                FUManager.share()?.loadItem(virtualAppearance.item, completion: nil);
            }
        }
    }
    
    func reset() {
        func perSkinReset(tag: String) {
            guard let param = VideoEnhancement.getFUSkinParam(with: tag) else {
                return
            }
            
            param.mValue = param.defaultValue
            FUManager.share()?.setParamItemAboutType(.typeBeauty, name: param.mParam, value: param.mValue)
        }
        
        func perShapReset(tag: String) {
            guard let param = VideoEnhancement.getFUShapeParam(with: tag) else {
                return
            }
            
            param.mValue = param.defaultValue
            FUManager.share()?.setParamItemAboutType(.typeBeauty, name: param.mParam, value: param.mValue)
        }
        
        perSkinReset(tag: "blur_level")
        perSkinReset(tag: "color_level")
        
        perShapReset(tag: "cheek_thinning")
        perShapReset(tag: "eye_enlarging")
        
        beauty = .off
        virtualAppearance = .none
    }
}
