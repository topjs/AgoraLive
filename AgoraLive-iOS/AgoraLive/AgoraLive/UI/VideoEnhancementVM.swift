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
    private let bag = DisposeBag()
    
    // Rx
    lazy var beauty = BehaviorRelay(value: enhancement.beauty)
    lazy var smooth = BehaviorRelay(value: enhancement.getFilterItem(with: .smooth).value)
    lazy var brighten = BehaviorRelay(value: enhancement.getFilterItem(with: .brighten).value)
    
    var minSmooth: Float {
        return enhancement.getFilterItem(with: .smooth).minValue
    }
    
    var maxSmooth: Float {
        return enhancement.getFilterItem(with: .smooth).maxValue
    }
    
    var minBrighten: Float {
        return enhancement.getFilterItem(with: .brighten).minValue
    }
    
    var maxBrighten: Float {
        return enhancement.getFilterItem(with: .brighten).maxValue
    }
    
    var virtualAppearance: VirtualAppearance = .dog
    
    override init() {
        super.init()
        observerPropertys()
    }
}

extension VideoEnhancementVM {
    private var enhancement: VideoEnhancement {
       return  ALCenter.shared().centerProvideMediaHelper().enhancement
    }
    
    func beauty(_ action: AGESwitch) {
        switch action {
        case .on:
            self.enhancement.beauty(.on, success: { [unowned self] in
                DispatchQueue.main.async { [unowned self] in
                    self.beauty.accept(.on)
                }
            }) { [unowned self] in
                DispatchQueue.main.async { [unowned self] in
                    self.beauty.accept(.off)
                }
            }
        case .off:
            self.enhancement.beauty(.off)
            self.beauty.accept(.off)
        }
    }

//
//    var cheekThining: Double {
//        set {
//            enhancement.cheekThining = newValue
//            publishCheekThing.accept(newValue)
//        }
//
//        get {
//            return enhancement.cheekThining
//        }
//    }
//
//    var eyeEnlarging: Double {
//        set {
//            enhancement.eyeEnlarging = newValue
//            publishEyeEnlarging.accept(newValue)
//        }
//
//        get {
//            return enhancement.eyeEnlarging
//        }
//    }
//
    
    
    func reset() {
        enhancement.reset()
    }
}

extension VideoEnhancementVM {
    func observerPropertys() {
        smooth.subscribe(onNext: { [unowned self] (value) in
            self.enhancement.setFilterValue(Float(value), with: .smooth)
        }).disposed(by: bag)
        
        brighten.subscribe(onNext: { [unowned self] (value) in
            self.enhancement.setFilterValue(Float(value), with: .brighten)
        }).disposed(by: bag)
    }
}
