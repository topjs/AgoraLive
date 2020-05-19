//
//  AppDelegate.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/2/10.
//  Copyright © 2020 CavanSu. All rights reserved.
//

import UIKit
import Alamofire

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        let center = ALCenter.shared()
        center.appAssistant.checkMinVersion { [unowned center] in
            center.reinitAgoraServe()
            center.registerAndLogin()
        }
        return true
    }
}
