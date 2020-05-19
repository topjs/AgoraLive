//
//  MusicViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/3/31.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit

class MusicCell: UITableViewCell {
    @IBOutlet weak var tagImageView: UIImageView!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var singerLabel: UILabel!
    
    var isPlaying: Bool = false {
        didSet {
            self.contentView.backgroundColor = isPlaying ? UIColor(hexString: "#0088EB") : UIColor.white
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        let color = UIColor(hexString: "#D8D8D8")
        let x: CGFloat = 15.0
        let width = UIScreen.main.bounds.width - (x * 2)
        self.contentView.containUnderline(color,
                                          x: x,
                                          width: width)
    }
}

class MusicViewController: UITableViewController {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var musicLinkLabel: UILabel!
    
    var playingImage = UIImage(named: "icon-pause")
    var pauseImage = UIImage(named: "icon-play")
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let color = UIColor(hexString: "#D8D8D8")
        let x: CGFloat = 15.0
        let width = UIScreen.main.bounds.width - (x * 2)
        self.titleLabel.containUnderline(color,
                                         x: x,
                                         width: width)
        
        self.titleLabel.text = NSLocalizedString("BGM")
        
        self.tableView.rowHeight = 58.0
        
        let music = "Music: "
        let link = "https://www.bensound.com"
        
        let content = (music + link) as NSString
        let attrContent = NSMutableAttributedString(string: (content as String))
        
        attrContent.addAttributes([.foregroundColor: UIColor(hexString: "#333333"),
                                   .font: UIFont.systemFont(ofSize: 12)],
                                  range: NSRange(location: 0, length: music.count))
        
        attrContent.addAttributes([.foregroundColor: UIColor(hexString: "#0088EB"),
                                   .font: UIFont.systemFont(ofSize: 12)],
                                  range: NSRange(location: music.count, length: link.count))
        
        musicLinkLabel.attributedText = attrContent
        musicLinkLabel.backgroundColor = UIColor(hexString: "#EEEEEE")
        musicLinkLabel.cornerRadius(4)
        musicLinkLabel.layer.masksToBounds = true
    }
}
