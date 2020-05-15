//
//  LiveListTabViewController.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/2/19.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import MJRefresh
import MBProgressHUD
import AgoraRtcKit

class LiveListTabViewController: MaskViewController, ShowAlertProtocol {
    @IBOutlet weak var tabView: TabSelectView!
    @IBOutlet weak var createButton: UIButton!
    
    private let listVM = LiveListVM()
    private let bag = DisposeBag()
    private var listVC: LiveListViewController?
    private var timer: Timer?
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        cancelScheduelRefresh()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        updateViews()
        
        // TabSelectView
        updateTabSelectView()
        // LiveListViewController
        updateLiveListVC()

        updateViewsWithListVM()
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        guard let segueId = segue.identifier else {
            return
        }
        
        switch segueId {
        case "LiveListViewController":
            listVC = segue.destination as? LiveListViewController
        case "CreateLiveViewController":
            let vc = segue.destination as? CreateLiveViewController
            vc?.liveType = self.listVM.presentingType
            vc?.publicRoomSettings.subscribe(onNext: { [unowned self] (settings) in
                DispatchQueue.main.async {
                    self.startLivingWithLocalSettings(settings)
                }
            }).disposed(by: bag)
        case "MultiBroadcastersViewController":
            guard let sender = sender,
                let info = sender as? LiveSession.JoinedInfo,
                let seatInfo = info.seatInfo else {
                fatalError()
            }
            
            let vc = segue.destination as? MultiBroadcastersViewController
            vc?.hidesBottomBarWhenPushed = true
            vc?.audienceListVM.updateGiftListWithJson(list: info.giftAudience)
            vc?.seatVM = try! LiveSeatVM(list: seatInfo)
        case "SingleBroadcasterViewController":
            guard let sender = sender,
                let info = sender as? LiveSession.JoinedInfo else {
                    fatalError()
            }
            
            let vc = segue.destination as? SingleBroadcasterViewController
            vc?.hidesBottomBarWhenPushed = true
            vc?.audienceListVM.updateGiftListWithJson(list: info.giftAudience)
        case "PKBroadcastersViewController":
            guard let sender = sender,
                let info = sender as? LiveSession.JoinedInfo else {
                    fatalError()
            }
            
            var statistics: PKStatistics
            
            if let pkInfo = info.pkInfo {
                statistics = try! PKStatistics(dic: pkInfo)
            } else {
                statistics = PKStatistics(state: .none)
            }
            
            let vc = segue.destination as? PKBroadcastersViewController
            vc?.hidesBottomBarWhenPushed = true
            vc?.pkVM = PKVM(statistics: statistics)
        default:
            break
        }
    }
}

private extension LiveListTabViewController {
    func updateViews() {
        self.createButton.layer.shadowOpacity = 1
        self.createButton.layer.shadowOffset = CGSize(width: 0, height: 3)
        self.createButton.layer.shadowColor = UIColor.black.cgColor
    }
    
    func updateTabSelectView() {
        let titles = LiveType.list.map { (item) -> String in
            return item.description
        }
        
        tabView.update(titles)
        
        tabView.selectedIndex.subscribe(onNext: { [unowned self] (index) in
            var type: LiveType
            
            switch index {
            case 0: type = .multiBroadcasters
            case 1: type = .singleBroadcaster
            case 2: type = .pkBroadcasters
            default: fatalError()
            }
            
            self.listVM.presentingType = type
            
            if self.listVM.presentingList.value.count == 0 {
                self.listVM.refetch()
            }
        }).disposed(by: bag)
    }
    
    func updateLiveListVC() {
        guard let vc = listVC else {
            fatalError()
        }
        
        // placeHolderView tap
        vc.placeHolderView.tap.subscribe(onNext: { [unowned self] (_) in
            self.scheduelRefresh()
        }).disposed(by: bag)
        
        // placeHolderView if need hidden
        listVM.presentingList.subscribe(onNext: { [unowned vc] (list) in
            vc.collectionView.isHidden = list.count == 0 ? true : false
        }).disposed(by: bag)
        
        // Cell Reload
        listVM.presentingList
            .bind(to: vc.collectionView.rx.items(cellIdentifier: "RoomCell",
                                                 cellType: RoomCell.self)) { index, item, cell in
                                                    cell.briefView.label.text = item.name
                                                    cell.personCountView.label.text = "\(item.personCount)"
                                                    cell.briefView.imageView.image = ALCenter.shared().centerProvideImagesHelper().getRoom(index: item.imageIndex)
        }.disposed(by: bag)
        
        // MJRefresh
        vc.collectionView.mj_header = MJRefreshNormalHeader(refreshingBlock: { [unowned self, unowned vc] in
            self.listVM.refetch(success: {
                vc.collectionView.mj_header?.endRefreshing()
            }) { // fail
                vc.collectionView.mj_header?.endRefreshing()
            }
        })
        
        vc.collectionView.mj_footer = MJRefreshBackFooter(refreshingBlock: { [unowned self, unowned vc] in
            self.listVM.fetch(success: {
                vc.collectionView.mj_footer?.endRefreshing()
            }) { // fail
                vc.collectionView.mj_footer?.endRefreshing()
            }
        })
        
        vc.collectionView.rx.modelSelected(RoomBrief.self).subscribe(onNext: { [unowned self] (room) in
            let type = self.listVM.presentingType
            var settings = LocalLiveSettings(title: room.name)
            switch type {
            case .multiBroadcasters:
                var media = settings.media
                media.resolution = AgoraVideoDimension240x240
                media.frameRate = .fps15
                media.bitRate = 200
                settings.media = media
            default:
                break
            }
            
            let session = LiveSession(roomId: room.roomId,
                                      settings: settings,
                                      type: type)
            self.joinLiving(session: session)
        }).disposed(by: bag)
        
        vc.collectionView.rx.willBeginDragging.subscribe(onNext: { [unowned self] in
            self.cancelScheduelRefresh()
        }).disposed(by: bag)
        
        vc.collectionView.rx.didEndDragging.subscribe(onNext: { [unowned self] (done) in
            if done {
                self.perMinuterRefresh()
            }
        }).disposed(by: bag)
    }
    
    func updateViewsWithListVM() {
        listVM.refetch()
    }
    
    func perMinuterRefresh() {
        timer = Timer(fireAt: Date(timeIntervalSinceNow: 60.0),
                      interval: 60.0,
                      target: self,
                      selector: #selector(scheduelRefresh),
                      userInfo: nil,
                      repeats: true)
        RunLoop.main.add(timer!, forMode: .common)
        timer?.fire()
    }
    
    @objc func scheduelRefresh() {
        let end: Completion = { [unowned self] in
            self.hiddenHUD()
        }

        self.showHUD()
        listVM.refetch(success: end, fail: end)
    }
    
    func cancelScheduelRefresh() {
        timer?.invalidate()
        timer = nil
    }
}

extension LiveListTabViewController {
    func startLivingWithLocalSettings(_ settings: LocalLiveSettings) {
        self.showHUD()
        
        let center = ALCenter.shared()
        center.createLiveSession(roomSettings: settings,
                                 type: self.listVM.presentingType,
                                 success: { [unowned self] (session) in
                                    self.joinLiving(session: session)
        }) { [unowned self] in
            self.hiddenHUD()
            self.showAlert(message:"start live fail")
        }
    }
    
    func joinLiving(session: LiveSession) {
        self.showHUD()
        
        let center = ALCenter.shared()
        center.liveSession = session
        session.join(success: { [unowned session, unowned self] (info: LiveSession.JoinedInfo) in
            self.hiddenHUD()
            
            switch session.type {
            case .multiBroadcasters:
                self.performSegue(withIdentifier: "MultiBroadcastersViewController", sender: info)
            case .singleBroadcaster:
                self.performSegue(withIdentifier: "SingleBroadcasterViewController", sender: info)
            case .pkBroadcasters:
                self.performSegue(withIdentifier: "PKBroadcastersViewController", sender: info)
            }
        }) {
            self.hiddenHUD()
            self.showAlert(message:"join live fail")
        }
    }
}
