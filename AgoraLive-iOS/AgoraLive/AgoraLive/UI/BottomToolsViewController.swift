//
//  BottomToolsViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/3/27.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit

class BottomToolsViewController: UIViewController {
    lazy var textInput = TextInputView()
    lazy var beautyButton = UIButton()
    lazy var extensionButton = UIButton()
    lazy var musicButton = UIButton()
    lazy var closeButton = UIButton()
    lazy var giftButton = UIButton()
    lazy var superRenderButton = UIButton()
    
    var tintColor: UIColor = .black {
        didSet {
            textInput.backgroundColor = tintColor
            closeButton.backgroundColor = tintColor
            extensionButton.backgroundColor = tintColor
            beautyButton.backgroundColor = tintColor
            giftButton.backgroundColor = tintColor
            musicButton.backgroundColor = tintColor
            superRenderButton.backgroundColor = tintColor
            giftButton.backgroundColor = tintColor
        }
    }
    var liveType: LiveType = .multiBroadcasters
    var perspective: LiveRoleType = .owner
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .clear
        
        textInput.returnKeyType = .done
        textInput.attributedPlaceholder = NSAttributedString(string: NSLocalizedString("Live_Text_Input_Placeholder"),
                                                             attributes: [NSAttributedString.Key.foregroundColor: UIColor(hexString: "#CCCCCC"),
                                                                          NSAttributedString.Key.font: UIFont.systemFont(ofSize: 14)])
        self.view.addSubview(textInput)
        
        closeButton.setImage(UIImage(named: "icon-close-white"), for: .normal)
        self.view.addSubview(closeButton)
        
        extensionButton.setImage(UIImage(named: "icon-more-white"), for: .normal)
        self.view.addSubview(extensionButton)
        
        switch (liveType, perspective) {
        case (_, .owner):
            beautyButton.setImage(UIImage(named: "icon-beauty"), for: .normal)
            beautyButton.setImage(UIImage(named:"icon-beauty-active"), for: .selected)
            musicButton.setImage(UIImage(named:"icon-music"), for: .normal)
            musicButton.setImage(UIImage(named:"icon-music-active"), for: .selected)
            
            self.view.addSubview(beautyButton)
            self.view.addSubview(musicButton)
        case (.multiBroadcasters, .broadcaster):
            beautyButton.setImage(UIImage(named:"icon-beauty"), for: .normal)
            beautyButton.setImage(UIImage(named:"icon-beauty-active"), for: .selected)
            self.view.addSubview(beautyButton)
            
            giftButton.setImage(UIImage(named:"icon-gift"), for: .normal)
            self.view.addSubview(giftButton)
        case (.pkBroadcasters, .audience):   fallthrough
        case (.multiBroadcasters, .audience):
            giftButton.setImage(UIImage(named:"icon-gift"), for: .normal)
            self.view.addSubview(giftButton)
        case (.singleBroadcaster, .audience):
            superRenderButton.setImage(UIImage(named: "icon-resolution"), for: .normal)
            superRenderButton.setImage(UIImage(named:"icon-resolution-active"), for: .selected)
            self.view.addSubview(superRenderButton)
            
            giftButton.setImage(UIImage(named:"icon-gift"), for: .normal)
            self.view.addSubview(giftButton)
        default: fatalError()
        }
        
        tintColor = .black
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        let buttonWH: CGFloat = 38.0
        let space: CGFloat = 15.0
        let viewWidth = self.view.bounds.width
        let viewHeight = self.view.bounds.height
        
        closeButton.frame = CGRect(x:viewWidth - buttonWH - space,
                                   y: 0,
                                   width: buttonWH,
                                   height: buttonWH)
        closeButton.isCycle = true
        
        extensionButton.frame = CGRect(x: closeButton.frame.minX - space - buttonWH,
                                       y: 0,
                                       width: buttonWH,
                                       height: buttonWH)
        extensionButton.isCycle = true
        
        var lastButton: UIButton
        
        switch (liveType, perspective) {
        case (.multiBroadcasters, .owner): fallthrough
        case (.pkBroadcasters, .owner):    fallthrough
        case (.singleBroadcaster, .owner):
            musicButton.frame = CGRect(x: extensionButton.frame.minX - space - buttonWH,
                                       y: 0,
                                       width: buttonWH,
                                       height: buttonWH)
            musicButton.isCycle = true
            
            beautyButton.frame = CGRect(x: musicButton.frame.minX - space - buttonWH,
                                        y: 0,
                                        width: buttonWH,
                                        height: buttonWH)
            beautyButton.isCycle = true
            
            lastButton = beautyButton
        case (.multiBroadcasters, .broadcaster):
            giftButton.frame = CGRect(x: extensionButton.frame.minX - space - buttonWH,
                                      y: 0,
                                      width: buttonWH,
                                      height: buttonWH)
            giftButton.isCycle = true
            
            beautyButton.frame = CGRect(x: giftButton.frame.minX - space - buttonWH,
                                        y: 0,
                                        width: buttonWH,
                                        height: buttonWH)
            beautyButton.isCycle = true
            
            lastButton = beautyButton
        case (.pkBroadcasters, .audience):    fallthrough
        case (.multiBroadcasters, .audience):
            giftButton.frame = CGRect(x: extensionButton.frame.minX - space - buttonWH,
                                      y: 0,
                                      width: buttonWH,
                                      height: buttonWH)
            giftButton.isCycle = true
            
            lastButton = giftButton
        case (.singleBroadcaster, .audience):
            superRenderButton.frame = CGRect(x: extensionButton.frame.minX - space - buttonWH,
                                             y: 0,
                                             width: buttonWH,
                                             height: buttonWH)
            superRenderButton.isCycle = true
            
            giftButton.frame = CGRect(x: superRenderButton.frame.minX - space - buttonWH,
                                      y: 0,
                                      width: buttonWH,
                                      height: buttonWH)
            giftButton.isCycle = true
            
            lastButton = giftButton
        default: fatalError()
        }
        
        textInput.frame = CGRect(x: space,
                                 y: 0,
                                 width: lastButton.frame.minX - (space * 2),
                                 height: viewHeight)
        textInput.cornerRadius(viewHeight * 0.5)
    }
}
