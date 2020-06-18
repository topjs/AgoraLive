//
//  AboutViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/6/18.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit

class DisclaimerViewController: UIViewController {
    @IBOutlet weak var textView: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.title = NSLocalizedString("Test_Product_Disclaimer")
        self.textView.text = NSLocalizedString("Disclaimer_Detail")
    }
}

class AboutViewController: UITableViewController {
    @IBOutlet weak var privacyLabel: UILabel!
    @IBOutlet weak var disclaimerLabel: UILabel!
    @IBOutlet weak var registerLabel: UILabel!
    @IBOutlet weak var versionLabel: UILabel!
    @IBOutlet weak var sdkLabel: UILabel!
    @IBOutlet weak var alLabel: UILabel!
    
    @IBOutlet weak var releaseDateValueLabel: UILabel!
    @IBOutlet weak var sdkValueLabel: UILabel!
    @IBOutlet weak var alValueLabel: UILabel!
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        guard let navigation = self.navigationController as? CSNavigationController else {
            fatalError()
        }
        
        navigation.navigationBar.isHidden = false
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.title = NSLocalizedString("About")
        privacyLabel.text = NSLocalizedString("Privacy_Item")
        disclaimerLabel.text = NSLocalizedString("Disclaimer")
        registerLabel.text = NSLocalizedString("Register_Agora_Account")
        versionLabel.text = NSLocalizedString("Version_Release_Date")
        sdkLabel.text = NSLocalizedString("RTC_SDK_Version")
        alLabel.text = NSLocalizedString("AL_Version")
        
        alValueLabel.text = "Ver \(AppAssistant.version)"
        sdkValueLabel.text = "Ver \(ALCenter.shared().centerProvideMediaHelper().rtcVersion)"
        
        releaseDateValueLabel.text = "2020.6.18"
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        
        switch indexPath.row {
        case 0:
            var privacyURL: URL?
            if DeviceAssistant.Language.isChinese {
                privacyURL = URL(string: "https://www.agora.io/cn/privacy-policy/")
            } else {
                privacyURL = URL(string: "https://www.agora.io/en/privacy-policy/")
            }
            
            guard let url = privacyURL else {
                return
            }
            
            UIApplication.shared.openURL(url)
        case 1:
            break
        case 2:
            var privacyURL: URL?
            if DeviceAssistant.Language.isChinese {
                privacyURL = URL(string: "https://sso.agora.io/cn/signup/")
            } else {
                privacyURL = URL(string: "https://sso.agora.io/en/signup/")
            }
            
            guard let url = privacyURL else {
                return
            }
            
            UIApplication.shared.openURL(url)
        default:
            break
        }
    }
}
