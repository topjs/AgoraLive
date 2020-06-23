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
    lazy var thinning = BehaviorRelay(value: enhancement.getFilterItem(with: .thinning).value)
    lazy var eyeEnlarge = BehaviorRelay(value: enhancement.getFilterItem(with: .eye).value)
    
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
    
    var minThinning: Float {
        return enhancement.getFilterItem(with: .thinning).minValue
    }
    
    var maxThinning: Float {
        return enhancement.getFilterItem(with: .thinning).maxValue
    }
    
    var minEyeEnlarge: Float {
        return enhancement.getFilterItem(with: .eye).minValue
    }
    
    var maxEyeEnlarge: Float {
        return enhancement.getFilterItem(with: .eye).maxValue
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
            smooth.accept(enhancement.getFilterItem(with: .smooth).value)
            brighten.accept(enhancement.getFilterItem(with: .brighten).value)
            thinning.accept(enhancement.getFilterItem(with: .thinning).value)
            eyeEnlarge.accept(enhancement.getFilterItem(with: .eye).value)
        }
    }
    
    func reset() {
        beauty(.off)
    }
}

extension VideoEnhancementVM {
    func observerPropertys() {
        smooth.subscribe(onNext: { [unowned self] (value) in
            self.enhancement.setFilterValue(value, with: .smooth)
        }).disposed(by: bag)
        
        brighten.subscribe(onNext: { [unowned self] (value) in
            self.enhancement.setFilterValue(value, with: .brighten)
        }).disposed(by: bag)
        
        thinning.subscribe(onNext: { [unowned self] (value) in
            self.enhancement.setFilterValue(value, with: .thinning)
        }).disposed(by: bag)
        
        eyeEnlarge.subscribe(onNext: { [unowned self] (value) in
            self.enhancement.setFilterValue(value, with: .eye)
        }).disposed(by: bag)
    }
}
