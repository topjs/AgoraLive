//
//  BeautySettingsViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/3/12.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift

class BeautySettingsViewController: UITableViewController {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var workSwitch: UISwitch!
    
    @IBOutlet weak var nameLabel1: UILabel!
    @IBOutlet weak var nameLabel2: UILabel!
    @IBOutlet weak var nameLabel3: UILabel!
    @IBOutlet weak var nameLabel4: UILabel!
    
    @IBOutlet weak var valueLabel1: UILabel!
    @IBOutlet weak var valueLabel2: UILabel!
    @IBOutlet weak var valueLabel3: UILabel!
    @IBOutlet weak var valueLabel4: UILabel!
    
    @IBOutlet weak var slider1: UISlider!
    @IBOutlet weak var slider2: UISlider!
    @IBOutlet weak var slider3: UISlider!
    @IBOutlet weak var slider4: UISlider!
    
    private let bag = DisposeBag()
    let enhanceVM = VideoEnhancementVM()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let color = UIColor(hexString: "#D8D8D8")
        let x: CGFloat = 15.0
        let width = UIScreen.main.bounds.width - (x * 2)
        self.titleLabel.containUnderline(color,
                                         x: x,
                                         width: width)
        
        self.titleLabel.text = NSLocalizedString("Beauty")
        
        self.nameLabel1.text = NSLocalizedString("BlurLevel")
        self.nameLabel2.text = NSLocalizedString("ColorLevel")
        self.nameLabel3.text = NSLocalizedString("CheekThining")
        self.nameLabel4.text = NSLocalizedString("EyeEnlarging")
        
        self.nameLabel1.adjustsFontSizeToFitWidth = true
        self.nameLabel2.adjustsFontSizeToFitWidth = true
        self.nameLabel3.adjustsFontSizeToFitWidth = true
        self.nameLabel4.adjustsFontSizeToFitWidth = true
        
        enhanceVM.publishWork.subscribe(onNext: { [unowned self] (work) in
            self.workSwitch.isOn = work.boolValue
            self.slider1.isEnabled = work.boolValue
            self.slider2.isEnabled = work.boolValue
            self.slider3.isEnabled = work.boolValue
            self.slider4.isEnabled = work.boolValue
        }).disposed(by: bag)
        
        enhanceVM.publishBlur.subscribe(onNext: { [unowned self] (value) in
            self.slider1.value = Float(value)
            self.valueLabel1.text = String(format: "%0.1f", value)
        }).disposed(by: bag)
        
        
        enhanceVM.publishColor.subscribe(onNext: { [unowned self] (value) in
            self.slider2.value = Float(value)
            self.valueLabel2.text = String(format: "%0.1f", value)
        }).disposed(by: bag)
      
        enhanceVM.publishCheekThing.subscribe(onNext: { [unowned self] (value) in
            self.slider3.value = Float(value)
            self.valueLabel3.text = String(format: "%0.1f", value)
        }).disposed(by: bag)
        
        enhanceVM.publishEyeEnlarging.subscribe(onNext: { [unowned self] (value) in
            self.slider4.value = Float(value)
            self.valueLabel4.text = String(format: "%0.1f", value)
        }).disposed(by: bag)
        
        self.workSwitch.rx.value.subscribe(onNext: { [unowned self] (value) in
            self.enhanceVM.beauty = value ? .on : .off
        }).disposed(by: bag)
        
        self.slider1.rx.value.subscribe(onNext: { [unowned self] (value) in
            self.enhanceVM.blurLevel = Double(value)
        }).disposed(by: bag)
        
        self.slider2.rx.value.subscribe(onNext: { [unowned self] (value) in
            self.enhanceVM.colorLevel = Double(value)
        }).disposed(by: bag)
        
        self.slider3.rx.value.subscribe(onNext: { [unowned self] (value) in
            self.enhanceVM.cheekThining = Double(value)
        }).disposed(by: bag)
        
        self.slider4.rx.value.subscribe(onNext: { [unowned self] (value) in
            self.enhanceVM.eyeEnlarging = Double(value)
        }).disposed(by: bag)
    }
}
