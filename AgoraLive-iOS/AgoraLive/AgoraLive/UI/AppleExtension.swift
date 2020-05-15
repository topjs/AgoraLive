//
//  AppleExtension.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/2/19.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import MBProgressHUD

public func NSLocalizedString(_ key: String) -> String {
    return NSLocalizedString(key, comment: "")
}

extension UIView {
    func cornerRadius(_ value: CGFloat) {
        self.layer.cornerRadius = value
    }
    
    var isCycle: Bool {
        get {
            return self.bounds.height == self.layer.cornerRadius * 2
        }
        set {
            guard self.bounds.height == self.bounds.width else {
                return
            }
            
            self.layer.cornerRadius = self.bounds.height * 0.5
        }
    }
    
    @discardableResult func containUnderline(_ color: UIColor, x: CGFloat? = nil, width: CGFloat? = nil, height: CGFloat? = nil) -> CALayer {
        let underline = CALayer()
        underline.backgroundColor = color.cgColor
        
        var tX: CGFloat = 0.0
        var h: CGFloat = 1.0
        var w: CGFloat = self.bounds.width
        
        if let x = x {
            tX = x
        }
        
        if let height = height {
            h = height
        }
        
        if let width = width {
            w = width
        }
        
        underline.frame = CGRect(x: tX,
                                 y: self.bounds.height,
                                 width: w,
                                 height: h)
        
        self.layer.addSublayer(underline)
        return underline
    }
}

extension String {
    func size(font: UIFont, drawRange size: CGSize) -> CGSize {
        let attributes = [NSAttributedString.Key.font: font]
        let option = NSStringDrawingOptions.usesLineFragmentOrigin
        let rect = self.boundingRect(with: size,
                                     options: option,
                                     attributes: attributes,
                                     context: nil)
        return rect.size
    }
}

extension UIScreen {
    var isNotch: Bool {
        if bounds.height == 896.0 || bounds.height == 812.0 {
            return true
        } else {
            return false
        }
    }
    
    var heightOfSafeAreaTop: CGFloat {
        return (self.isNotch ? 44 : 20)
    }
    
    var heightOfSafeAreaBottom: CGFloat {
        return (self.isNotch ? 34 : 0)
    }
}

extension TimeInterval {
    static let animation: TimeInterval = 0.3
}

extension UIViewController {
    func presentChild(_ viewController: UIViewController, animated flag: Bool, presentedFrame: CGRect) {
        self.view.addSubview(viewController.view)
        
        let originRect = CGRect(x: presentedFrame.origin.x,
                                y: UIScreen.main.bounds.height,
                                width: presentedFrame.width,
                                height: presentedFrame.height)
        viewController.view.frame = originRect
        
        if flag {
            UIView.animate(withDuration: TimeInterval.animation) {
                viewController.view.frame = presentedFrame
            }
        } else {
            viewController.view.frame = presentedFrame
        }
        
        self.addChild(viewController)
        viewController.didMove(toParent: self)
    }
    
    func dismissChild(_ viewController: UIViewController, animated flag: Bool) {
        var endRect = viewController.view.frame
        endRect.origin.y = UIScreen.main.bounds.height
        
        viewController.willMove(toParent: nil)
        
        if flag {
            UIView.animate(withDuration: TimeInterval.animation, animations: {
                viewController.view.frame = endRect
            }) { (finish) in
                guard finish else {
                    return
                }
                viewController.view.removeFromSuperview()
                viewController.removeFromParent()
            }
        } else {
            viewController.view.frame = endRect
            viewController.view.removeFromSuperview()
            viewController.removeFromParent()
        }
    }
}

class MaskViewController: UIViewController {
    private var hud: MBProgressHUD?
    private var maskTapBlock: (() -> Void)?
    private var toastView: UIView?
    private var toastWork: AfterWorker?
    
    private(set) var maskView: UIControl = {
        let view = UIControl()
        view.isSelected = false
        view.backgroundColor = UIColor(red: 0.0,
                                       green: 0.0,
                                       blue: 0.0,
                                       alpha: 0.7)
        let w = UIScreen.main.bounds.width
        let h = UIScreen.main.bounds.height
        view.frame = CGRect(x: 0,
                            y: 0,
                            width: w,
                            height: h)
        view.addTarget(self, action: #selector(tapMaskView(_:)), for: .touchUpInside)
        view.isHidden = true
        return view
    }()
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        view.endEditing(true)
    }
    
    func showMaskView(color: UIColor = UIColor(red: 0.0,
                                               green: 0.0,
                                               blue: 0.0,
                                               alpha: 0.7), tap: (() -> Void)? = nil) {
        self.maskTapBlock = tap
        maskView.isHidden = false
        maskView.backgroundColor = color
        self.view.addSubview(maskView)
    }
    
    func hiddenMaskView() {
        self.maskTapBlock = nil
        maskView.isHidden = true
        maskView.removeFromSuperview()
    }
    
    @objc private func tapMaskView(_ mask: UIControl) {
        if let maskTapBlock = maskTapBlock {
            maskTapBlock()
        }
    }
    
    func showHUD() {
        guard self.hud == nil else {
            return
        }
        
        self.hud = MBProgressHUD.showAdded(to: self.view, animated: true)
        self.hud?.show(animated: true)
    }
    
    func hiddenHUD() {
        self.hud?.hide(animated: true)
        self.hud = nil
    }
    
    func showToastView(_ view: UIView? = nil, position: CGPoint, duration: TimeInterval = TimeInterval.animation) {
        guard let window = UIApplication.shared.keyWindow else {
            return
        }
        
        if let worker = toastWork {
            self.toastView?.removeFromSuperview()
            worker.cancel()
        }
        
        toastWork = AfterWorker()
        
        if view == nil {
            self.toastView = ToastView(frame: CGRect.zero)
        } else {
            self.toastView = view
        }
        
        self.toastView?.center = position
        window.addSubview(self.toastView!)
        
        toastWork?.perform(after: duration, on: DispatchQueue.main, { [unowned self] in
            self.toastView?.removeFromSuperview()
        })
    }
}

protocol ShowAlertProtocol: UIViewController {
    func showAlert(_ title: String?, message: String?, handler: ((UIAlertAction) -> Void)?)
    func showAlert(_ title: String?, message: String?, preferredStyle: UIAlertController.Style, action1: String, action2: String, handler1: ((UIAlertAction) -> Void)?, handler2: ((UIAlertAction) -> Void)?)
    func showAlert(_ title: String?, message: String?, preferredStyle: UIAlertController.Style, actions: [UIAlertAction]?, completion: Completion)
}

extension ShowAlertProtocol {
    func showAlert(_ title: String? = nil, message: String? = nil, handler: ((UIAlertAction) -> Void)? = nil) {
        let action = UIAlertAction(title: "OK", style: .default, handler: handler)
        showAlert(title, message: message, preferredStyle: .alert, actions: [action], completion: nil)
    }
    
    func showAlert(_ title: String? = nil, message: String? = nil, preferredStyle: UIAlertController.Style = .alert, action1: String, action2: String, handler1: ((UIAlertAction) -> Void)? = nil, handler2: ((UIAlertAction) -> Void)? = nil) {
        let act1 = UIAlertAction(title: action1, style: .default, handler: handler1)
        let act2 = UIAlertAction(title: action2, style: .default, handler: handler2)
        showAlert(title, message: message, preferredStyle: preferredStyle, actions: [act1, act2], completion: nil)
    }
    
    func showAlert(_ title: String? = nil, message: String? = nil, preferredStyle: UIAlertController.Style = .alert, actions: [UIAlertAction]? = nil, completion: Completion) {
        view.endEditing(true)
        
        let alert = UIAlertController(title: title, message: message, preferredStyle: preferredStyle)
        var tActions: [UIAlertAction]
        
        if let actions = actions {
            tActions = actions
        } else {
            tActions = [UIAlertAction(title: "OK", style: .default, handler: nil)]
        }
        
        for item in tActions {
            alert.addAction(item)
        }
        
        present(alert, animated: true, completion: completion)
    }
}

extension UIStoryboard {
    static func initViewController<T: Any>(of id: String, class: T.Type, on stroyName: String = "Main") -> T {
        let storyboard = UIStoryboard(name: stroyName, bundle: Bundle.main)
        let identifier = id
        let vc = storyboard.instantiateViewController(withIdentifier: identifier) as! T
        return vc
    }
}

extension NotificationCenter {
    func observerKeyboard(listening: (((endFrame: CGRect, duration: Double)) -> Void)? = nil) {
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillChangeFrameNotification, object: nil, queue: nil) { (notify) in
            guard let userInfo = notify.userInfo else {
                return
            }
            
            let endKeyboardFrameValue = userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue
            let endKeyboardFrame = endKeyboardFrameValue?.cgRectValue
            let durationValue = userInfo[UIResponder.keyboardAnimationDurationUserInfoKey] as? NSNumber
            let duration = durationValue?.doubleValue
            
            if let listening = listening {
                let callbackParameter = (endFrame: endKeyboardFrame!, duration: duration!)
                listening(callbackParameter)
            }
        }
    }
}
