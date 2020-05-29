//
//  VideoEnhancementVM.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/4/28.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import RxRelay

class VideoEnhancementVM: NSObject {
    // Rx
    lazy var publishWork = BehaviorRelay(value: enhancement.beauty)
    lazy var publishBlur = BehaviorRelay(value: enhancement.blurLevel)
    lazy var publishColor = BehaviorRelay(value: enhancement.colorLevel)
    lazy var publishCheekThing = BehaviorRelay(value: enhancement.cheekThining)
    lazy var publishEyeEnlarging = BehaviorRelay(value: enhancement.eyeEnlarging)
}

extension VideoEnhancementVM {
    private var enhancement: VideoEnhancement {
       return  ALCenter.shared().centerProvideMediaHelper().enhancement
    }
    
    var beauty: AGESwitch {
        set {
            enhancement.beauty = newValue
            publishWork.accept(newValue)
        }
        
        get {
            return enhancement.beauty
        }
    }
    
    var blurLevel: Double {
        set {
            enhancement.blurLevel = newValue
            publishBlur.accept(newValue)
        }
        
        get {
            return enhancement.blurLevel
        }
    }
    
    var colorLevel: Double {
        set {
            enhancement.colorLevel = newValue
            publishColor.accept(newValue)
        }
        
        get {
            return enhancement.colorLevel
        }
    }
    
    var cheekThining: Double {
        set {
            enhancement.cheekThining = newValue
            publishCheekThing.accept(newValue)
        }
        
        get {
            return enhancement.cheekThining
        }
    }
    
    var eyeEnlarging: Double {
        set {
            enhancement.eyeEnlarging = newValue
            publishEyeEnlarging.accept(newValue)
        }
        
        get {
            return enhancement.eyeEnlarging
        }
    }
    
    var virtualAppearance: VirtualAppearance {
        set {
            enhancement.virtualAppearance = newValue
        }
        
        get {
            return enhancement.virtualAppearance
        }
    }
    
    func reset() {
        enhancement.reset()
    }
}
