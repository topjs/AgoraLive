//
//  LiveListViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/2/21.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa

class PlaceHolderView: UIView {
    @IBOutlet var imageView: UIImageView!
    @IBOutlet var label: UILabel!
    
    var tap = PublishSubject<Bool>()
    
    @IBAction func doTapPressed(_ sender: UITapGestureRecognizer) {
        tap.onNext(true)
    }
}

class RoomCell: UICollectionViewCell {
    @IBOutlet weak var personCountView: IconTextView!
    @IBOutlet weak var briefView: LabelShadowView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.personCountView.offsetLeftX = -4.0
        self.personCountView.offsetRightX = 4.0
        self.personCountView.imageView.image = UIImage(named: "icon-audience")
        self.personCountView.label.textColor = UIColor.white
        self.personCountView.label.font = UIFont.systemFont(ofSize: 10)
    }
}

class LiveListViewController: UIViewController {
    @IBOutlet weak var placeHolderView: PlaceHolderView!
    @IBOutlet weak var collectionView: UICollectionView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        collectionView.isHidden = true
        
        let screenWidth = UIScreen.main.bounds.width
        let itemWidth = (screenWidth - (15 * 3)) * 0.5
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: itemWidth,
                                 height: itemWidth)
        
        collectionView.contentInset = UIEdgeInsets(top: 0,
                                                   left: 15,
                                                   bottom: 0,
                                                   right: 15)
        
        collectionView.setCollectionViewLayout(layout, animated: true)
        
        placeHolderView.label.text = NSLocalizedString("Create_A_Live_Room")
    }
}
