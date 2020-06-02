//
//  VirtualBroadcastersViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/5/29.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import RxRelay
import AGEVideoLayout

class VirtualBroadcastersViewController: MaskViewController, LiveViewController {
    @IBOutlet weak var ownerView: IconTextView!
    @IBOutlet weak var videoContainer: AGEVideoContainer!
    @IBOutlet weak var inviteButton: UIButton!
    
    private var ownerRenderView = UIView()
    
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
    var virtualVM = VirtualVM()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        videoContainer.backgroundColor = UIColor(hexString: "#DBEFFF")
        
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

extension VirtualBroadcastersViewController {
    // MARK: - Live Room
    func liveRoom(session: LiveSession) {
        guard let localRole = session.role else {
            fatalError()
        }
        
        let images = ALCenter.shared().centerProvideImagesHelper()
        
        ownerView.offsetLeftX = -13
        ownerView.offsetRightX = 5
        ownerView.label.textColor = .white
        ownerView.label.font = UIFont.systemFont(ofSize: 11)
        
        let owner = session.owner
        
        switch owner {
        case .localUser:
            ownerView.label.text = localRole.info.name
            ownerView.imageView.image = images.getHead(index: localRole.info.imageIndex)
            let owner = localRole as! LiveOwner
            playerVM.renderLocalVideoStream(id: owner.agoraUserId,
                                            view: self.ownerRenderView)
            deviceVM.camera = .on
            deviceVM.mic = .on
        case .otherUser(let remote):
            ownerView.label.text = remote.info.name
            ownerView.imageView.image = images.getHead(index: remote.info.imageIndex)
            playerVM.renderRemoteVideoStream(id: remote.agoraUserId,
                                             view: self.ownerRenderView)
            deviceVM.camera = .off
            deviceVM.mic = .off
        }
        
        updateVideoLayout()
        
        session.end.subscribe(onNext: { [unowned self] (_) in
            guard !owner.isLocal else {
                return
            }
            
            self.showAlert(NSLocalizedString("Live_End")) { [unowned self] (_) in
                self.leave()
            }
        }).disposed(by: bag)
        
        inviteButton.rx.tap.subscribe(onNext: { [unowned self] in
            self.presentInviteList()
        }).disposed(by: bag)
    }
    
    func vm(session: LiveSession) {
        
    }
    
    func updateVideoLayout() {
        let onlyOwner = true
        
        var layout: AGEVideoLayout
        
        if onlyOwner {
            layout = AGEVideoLayout(level: 0)
        } else {
            let width = UIScreen.main.bounds.width
            let height = width * 9 / 16
            
            layout = AGEVideoLayout(level: 0)
                .itemSize(.constant(CGSize(width: width, height: height)))
                .startPoint(x: 0, y: 160 + UIScreen.main.heightOfSafeAreaTop)
        }
    
        videoContainer.listItem { [unowned self] (index) -> AGEView in
            return self.ownerRenderView
        }
        
        videoContainer.listCount { [unowned self] (level) -> Int in
            return 1
        }
        
        videoContainer.setLayouts([layout], animated: true)
    }
    
    func presentInviteList() {
        guard let session = ALCenter.shared().liveSession else {
            return
        }
        
        showMaskView { [unowned self] in
            self.hiddenMaskView()
            if let vc = self.userListVC {
                self.dismissChild(vc, animated: true)
            }
        }
        
        presentUserList(listType: .broadcasting)
        
        let roomId = session.roomId
        
        self.userListVC?.selectedInviteAudience.subscribe(onNext: { [unowned self] (user) in
            guard let session = ALCenter.shared().liveSession else {
                return
            }
            
            self.hiddenMaskView()
            if let vc = self.userListVC {
                self.dismissChild(vc, animated: true)
                self.userListVC = nil
            }
            
            guard let role = session.role as? LiveOwner else {
                fatalError()
            }
            
//            self.seatVM.localOwner(role,
//                                   command: .invite,
//                                   on: seat,
//                                   with: user,
//                                   of: roomId) {[unowned self] (_) in
//                                    self.showAlert(message: NSLocalizedString("Invite_Broadcasting_Fail"))
//            }
        }).disposed(by: bag)
    }
    
    func presentRecievedApplyForBroadcasting() {
        self.showMaskView()
        
        self.showAlert(NSLocalizedString("Apply_For_Broadcasting"),
                       message: NSLocalizedString("Confirm_Apply_For_Broadcasting"),
                       action1: NSLocalizedString("Reject"), action2: NSLocalizedString("Confirm"),
                       handler1: { [unowned self] (_) in
                        self.hiddenMaskView()
        }) { (_) in
            self.hiddenMaskView()
        }
    }
    
    
    func presentRecievedBroadcastingInvitation() {
        self.showMaskView()
        
        self.showAlert(NSLocalizedString("Apply_For_Broadcasting"),
                       message: NSLocalizedString("Confirm_Accept_Broadcasting_Invitation"),
                       action1: NSLocalizedString("Reject"), action2: NSLocalizedString("Confirm"),
                       handler1: { [unowned self] (_) in
                        self.hiddenMaskView()
        }) { (_) in
            self.hiddenMaskView()
        }
    }
    
    func presentApplyForBroadcasting() {
        self.showAlert(NSLocalizedString("Apply_For_Broadcasting"),
                       message: NSLocalizedString("Confirm_Apply_For_Broadcasting"),
                       action1: NSLocalizedString("Cancel"), action2: NSLocalizedString("Confirm"),
                       handler1: { [unowned self] (_) in
                        self.hiddenMaskView()
        }) { (_) in
            self.hiddenMaskView()
        }
    }
}
