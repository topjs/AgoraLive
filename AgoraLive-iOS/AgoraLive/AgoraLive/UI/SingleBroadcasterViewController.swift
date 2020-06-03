//
//  SingleBroadcasterViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/4/13.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import RxRelay

class SingleBroadcasterViewController: MaskViewController, LiveViewController {
    @IBOutlet weak var ownerView: IconTextView!
    @IBOutlet weak var renderView: UIView!
    
    // LiveViewController
    var tintColor = UIColor(red: 0,
                            green: 0,
                            blue: 0,
                            alpha: 0.4)
    
    var bag: DisposeBag = DisposeBag()
    
     // ViewController
    var userListVC: UserListViewController?
    var giftAudienceVC: GiftAudienceViewController?
    var chatVC: ChatViewController?
    var bottomToolsVC: BottomToolsViewController?
    var beautyVC: BeautySettingsViewController?
    var musicVC: MusicViewController?
    var dataVC: RealDataViewController?
    var extensionVC: ExtensionViewController?
    var mediaSettingsNavi: UIViewController?
    var giftVC: GiftViewController?
    var gifVC: GIFViewController?
    
    // View
    @IBOutlet weak var personCountView: IconTextView!
    
    internal lazy var chatInputView: ChatInputView = {
        let chatHeight: CGFloat = 50.0
        let frame = CGRect(x: 0,
                           y: UIScreen.main.bounds.height,
                           width: UIScreen.main.bounds.width,
                           height: chatHeight)
        let view = ChatInputView(frame: frame)
        view.isHidden = true
        return view
    }()
    
    // ViewModel
    var audienceListVM = LiveRoomAudienceList()
    var musicVM = MusicVM()
    var chatVM = ChatVM()
    var giftVM = GiftVM()
    var deviceVM = MediaDeviceVM()
    var playerVM = PlayerVM()
    var enhancementVM = VideoEnhancementVM()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let image = UIImage(named: "live-bg")
        self.view.layer.contents = image?.cgImage
        
        guard let session = ALCenter.shared().liveSession else {
            fatalError()
        }
        
        liveRoom(session: session)
        audience()
        chatList()
        gift()
        
        bottomTools(session: session, tintColor: tintColor)
        chatInput()
        musicList()
        superResolution(session: session)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        guard let identifier = segue.identifier else {
            return
        }
        
        switch identifier {
        case "GiftAudienceViewController":
            let vc = segue.destination as! GiftAudienceViewController
            self.giftAudienceVC = vc
        case "BottomToolsViewController":
            guard let session = ALCenter.shared().liveSession else {
                fatalError()
            }
            
            guard let role = session.role else {
                fatalError()
            }
            
            let vc = segue.destination as! BottomToolsViewController
            vc.perspective = role.type
            vc.liveType = session.type
            self.bottomToolsVC = vc
        case "ChatViewController":
            let vc = segue.destination as! ChatViewController
            vc.cellColor = tintColor
            self.chatVC = vc
        default:
            break
        }
    }
}

extension SingleBroadcasterViewController {
    // MARK: - Live Room
    func liveRoom(session: LiveSession) {
        guard let owner = session.owner else {
            return
        }
        
        let images = ALCenter.shared().centerProvideImagesHelper()
        
        ownerView.offsetLeftX = -13
        ownerView.offsetRightX = 5
        ownerView.label.textColor = .white
        ownerView.label.font = UIFont.systemFont(ofSize: 11)
        
        switch owner {
        case .localUser(let user):
            ownerView.label.text = user.info.name
            ownerView.imageView.image = images.getHead(index: user.info.imageIndex)
            playerVM.renderLocalVideoStream(id: user.agoraUserId,
                                            view: self.renderView)
            deviceVM.camera = .on
            deviceVM.mic = .on
        case .otherUser(let remote):
            ownerView.label.text = remote.info.name
            ownerView.imageView.image = images.getHead(index: remote.info.imageIndex)
            playerVM.renderRemoteVideoStream(id: remote.agoraUserId,
                                             view: self.renderView)
            deviceVM.camera = .off
            deviceVM.mic = .off
        }
        
        session.end.subscribe(onNext: { [unowned self] (_) in
            guard !owner.isLocal else {
                return
            }
            
            self.showAlert(NSLocalizedString("Live_End")) { [unowned self] (_) in
                self.leave()
            }
        }).disposed(by: bag)
    }
    
    func superResolution(session: LiveSession) {
        bottomToolsVC?.superRenderButton.rx.tap.subscribe(onNext: { [unowned self, unowned session] () in
            guard let vc = self.bottomToolsVC else {
                fatalError()
            }
            
            vc.superRenderButton.isSelected.toggle()
            
            if vc.superRenderButton.isSelected {
                let view = SuperResolutionToast(frame: CGRect(x: 0, y: 0, width: 181, height: 44.0), filletRadius: 8)
                let point = CGPoint(x: UIScreen.main.bounds.width * 0.5, y: 200)
                self.showToastView(view, position: point, duration: 1.0)
            }
            
            switch session.owner {
            case .otherUser(let remote):
                let media = ALCenter.shared().centerProvideMediaHelper()
                media.player.renderRemoteVideoStream(id: remote.agoraUserId,
                                                     superResolution: vc.superRenderButton.isSelected ? .on : .off)
            default:
                fatalError()
            }
        }).disposed(by: bag)
    }
}
