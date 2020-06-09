//
//  UserNameViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/3/5.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import RxRelay

class UserNameViewController: UIViewController {
    @IBOutlet weak var nameTextField: UITextField!
    
    var newName: BehaviorRelay<String>!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        nameTextField.text = newName.value
        setupRightButton()
    }
    
    @objc func didDonePressed() {
        if let name = nameTextField.text, name != newName.value {
            newName.accept(name)
        }
        
        self.navigationController?.popViewController(animated: true)
    }
}

private extension UserNameViewController {
    func setupRightButton() {
        guard let navigation = self.navigationController as? CSNavigationController else {
            fatalError()
        }
        
        navigation.navigationBar.isHidden = false
        
        self.navigationItem.title = NSLocalizedString("Input_Name")
        
        let doneButton = UIButton(frame: CGRect(x: 0, y: 0, width: 69, height: 30))
        doneButton.addTarget(self, action: #selector(didDonePressed), for: .touchUpInside)
        doneButton.setTitle(NSLocalizedString("Done"), for: .normal)
        doneButton.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        doneButton.titleLabel?.adjustsFontSizeToFitWidth = true
        doneButton.setTitleColor(UIColor.white, for: .normal)
        doneButton.backgroundColor = UIColor(hexString: "#008AF3")
        doneButton.cornerRadius(4)
        navigation.rightButton = doneButton
    }
}
