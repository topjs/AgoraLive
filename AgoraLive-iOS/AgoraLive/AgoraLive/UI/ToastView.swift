//
//  ToastView.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/4/29.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit

class ToastView: UIView {
    
}

class SuperResolutionToast: FilletView {
    private var tagImageView = UIImageView(frame: CGRect.zero)
    private var label = UILabel(frame: CGRect.zero)
    private var labelSize = CGSize(width: 0, height: 20)
    private var leftSpace: CGFloat = 15.0
    private var topSpace: CGFloat = 15.0
    
    override init(frame: CGRect, filletRadius: CGFloat = 0.0) {
        super.init(frame: frame, filletRadius: filletRadius)
        self.initViews()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.initViews()
    }
    
    func initViews() {
        let text = NSLocalizedString("Super_Resolution_Enabled")
        let font = UIFont.systemFont(ofSize: 14)
        let newSize = text.size(font: font, drawRange: CGSize(width: CGFloat(MAXFLOAT), height: labelSize.height))
        self.labelSize = newSize
        
        self.insideBackgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.6)
        self.backgroundColor = .clear
        label.textAlignment = .left
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        label.text = text
        
        self.tagImageView.image = UIImage(named: "icon-done")
        
        self.addSubview(tagImageView)
        self.addSubview(label)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.tagImageView.frame = CGRect(x: leftSpace,
                                         y: 15,
                                         width: 14,
                                         height: 14)
        self.label.frame = CGRect(x: 33,
                                  y: 12,
                                  width: labelSize.width,
                                  height: labelSize.height)
        
        if (self.label.frame.maxX + leftSpace) != self.bounds.width {
            var newSize = self.frame.size
            newSize.width = self.label.frame.maxX + leftSpace
            self.frame.size = newSize
        }
    }
}
