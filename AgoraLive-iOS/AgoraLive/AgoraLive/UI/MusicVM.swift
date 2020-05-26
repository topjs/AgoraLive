//
//  MusicVM.swift
//  AgoraLive
//
//  Created by CavanSu on 2020/3/31.
//  Copyright © 2020 Agora. All rights reserved.
//

import UIKit
import RxSwift
import RxRelay

fileprivate extension Array where Element == Music {
    init(list: [StringAnyDic]) throws {
        var array = [Music]()
        
        for item in list {
            let music = try Music(dic: item)
            array.append(music)
        }
        
        self = array
    }
}

struct Music {
    var name: String
    var singer: String
    var isPlaying: Bool
    var url: String
    
    init(dic: StringAnyDic) throws {
        self.singer = try dic.getStringValue(of: "singer")
        self.name = try dic.getStringValue(of: "musicName")
        self.url = try dic.getStringValue(of: "url")
        self.isPlaying = false
    }
}

class MusicVM: NSObject {
    private let bag = DisposeBag()
    
    var listSelectedIndex: Int? {
        didSet {
            guard let index = listSelectedIndex else {
                isPlaying.accept(false)
                return
            }
            
            isPlaying.accept(true)
            playItem(oldValue, selected: index)
        }
    }
    
    var isPlaying = BehaviorRelay(value: false)
    
    // MARK: Rx
    // Output message
    var list: BehaviorRelay<[Music]>?
    
    func refetch() {
        let client = ALCenter.shared().centerProvideRequestHelper()
        let event = RequestEvent(name: "music-list")
        let url = URLGroup.musicList
        let task = RequestTask(event: event,
                               type: .http(.get, url: url),
                               timeout: .medium)
        
        let success: DicEXCompletion = { [unowned self] (json) in
            let data = try json.getListValue(of: "data")
            let list = try Array(list: data)
            self.list = BehaviorRelay(value: list)
        }
        
        let fail: ErrorRetryCompletion = { (error) in
            return .resign
        }
        
        let response = AGEResponse.json(success)
        client.request(task: task, success: response, failRetry: fail)
    }
}

private extension MusicVM {
    func playItem(_ last: Int?, selected: Int) {
        guard var musicList = self.list?.value else {
            fatalError()
        }
        
        let mediaKit = ALCenter.shared().centerProvideMediaHelper()
        
        defer {
            // send notification to view
            list?.accept(musicList)
        }
        
        var item = musicList[selected]
        item.isPlaying.toggle()
        musicList[selected] = item
        
        // pause / resume
        if let last = last, selected == last {
            if item.isPlaying {
                mediaKit.player.resumeMixFileAudio()
            } else {
                mediaKit.player.pauseMixFileAudio()
            }
            listSelectedIndex = nil
            return
        
        // cancel last playing state
        } else if let last = last {
            var lastItem = musicList[last]
            lastItem.isPlaying.toggle()
            musicList[last] = lastItem
        }
        
        // play
        mediaKit.player.startMixingFileAudio(url: item.url) { [unowned self] in
            guard let selectedIndex = self.listSelectedIndex else {
                fatalError()
            }
            let next = selectedIndex + 1
            self.listSelectedIndex = next
        }
    }
}
