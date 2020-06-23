//
//  FUClient.m
//  AgoraLive
//
//  Created by CavanSu on 2020/6/22.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "FUClient.h"
#import "FURenderer.h"
#import "authpack.h"

@implementation FUFilterItem
@end

@interface FUClient () {
    int items[10];
    int itemsCount;
    int frameID;
}

@property (nonatomic, strong) NSArray *filterItems;
@property (nonatomic, strong) dispatch_queue_t asyncLoadQueue;
@property (nonatomic, assign) BOOL loadAISuccess;
@property (nonatomic, assign) BOOL loadFilterSuccess;

@property (nonatomic, assign) int filterIndexOfItems;
@end

@implementation FUClient

- (instancetype)init {
    self = [super init];
    if (self) {
        [self initAll];
    }
    return self;
}

#pragma mark - Public
- (void)loadFilterWithSuccess:(FUCompletion)success fail:(FUErrorCompletion)fail {
    __weak typeof(self) weakSelf = self;
    
    dispatch_async(_asyncLoadQueue, ^{
        NSError *error = nil;
        if (![weakSelf checkLoadAIModel:&error]) {
            if (fail) {
                weakSelf.loadFilterSuccess = false;
                fail(error);
                return;
            }
        }
        
        int bundleIndex = 0;
        int result = [self loadModel:@"face_beautification.bundle" indexOfItems:&bundleIndex];
    
        if (result <= 0) {
            NSError *error = [NSError errorWithDomain:@"FU-load-face_beautification"
                                                 code:result
                                             userInfo:nil];
            if (fail) {
                weakSelf.loadFilterSuccess = false;
                fail(error);
                return;
            }
        }
        
        /* 默认精细磨皮 */
        __block int bundleHandle = self->items[bundleIndex];
        [FURenderer itemSetParam:bundleHandle withName:@"heavy_blur" value:@(0)];
        [FURenderer itemSetParam:bundleHandle withName:@"blur_type" value:@(2)];
        /* 默认自定义脸型 */
        [FURenderer itemSetParam:bundleHandle withName:@"face_shape" value:@(4)];
        
        weakSelf.filterIndexOfItems = bundleIndex;
        weakSelf.loadFilterSuccess = true;
        
        if (success) {
            success();
        }
    });
}

- (FUFilterItem *)getFilterItemWithType:(FUFilterItemType)type {
    FUFilterItem *instance = nil;
    for (int i = 0; i < self.filterItems.count; i++) {
        FUFilterItem *item = self.filterItems[i];
        if (item.type == type) {
            instance = item;
            break;
        }
    }
    return instance;
}

- (void)setFilterValue:(float)value withType:(FUFilterItemType)type {
    __block FUFilterItem *item = [self getFilterItemWithType:type];
    
    if (!item) {
        return;
    }
    
    if (value > item.maxValue
        || value < item.minValue
        || !self.loadFilterSuccess) {
        return;
    }
    
    __weak typeof(self) weakSelf = self;
    
    dispatch_async(_asyncLoadQueue, ^{
        int result = [FURenderer itemSetParam:self->items[weakSelf.filterIndexOfItems]
                                     withName:item.funcName
                                        value:@(value)];
        if (result > 0) {
            item.value = value;
        }
    });
}

- (void)destoryAllItems {
    [FURenderer destroyAllItems];
        
    for (int i = 0; i < itemsCount; i++) {
        items[i] = 0;
    }
    
    itemsCount = 0;
    
    FUFilterItem *smooth = [self getFilterItemWithType:FUFilterItemTypeSmooth];
    smooth.value = smooth.defaultValue;
    
    FUFilterItem *brighten = [self getFilterItemWithType:FUFilterItemTypeBrighten];
    brighten.value = brighten.defaultValue;
    
    FUFilterItem *thinning = [self getFilterItemWithType:FUFilterItemTypeThinning];
    thinning.value = thinning.defaultValue;
    
    FUFilterItem *eye = [self getFilterItemWithType:FUFilterItemTypeEye];
    eye.value = eye.defaultValue;
}

- (void)renderItemsToPixelBuffer:(CVPixelBufferRef)pixelBuffer {
    [[FURenderer shareRenderer] renderPixelBuffer:pixelBuffer
                                      withFrameId:frameID
                                            items:items
                                        itemCount:itemsCount
                                            flipx:true];
    
    frameID += 1;
}

#pragma mark - Private
- (void)initAll {
    itemsCount = 0;
    
    self.asyncLoadQueue = dispatch_queue_create("com.faceLoadItem", DISPATCH_QUEUE_SERIAL);
    
    [[FURenderer shareRenderer] setupWithData:nil
                                     dataSize:0
                                       ardata:nil
                                  authPackage:&g_auth_package
                                     authSize:sizeof(g_auth_package)
                          shouldCreateContext:YES];
    
    FUFilterItem *smooth = [[FUFilterItem alloc] init];
    smooth.type = FUFilterItemTypeSmooth;
    smooth.funcName = @"blur_level";
    smooth.minValue = 0;
    smooth.maxValue = 6.0;
    smooth.defaultValue = 6.0;
    smooth.value = smooth.defaultValue;
    
    FUFilterItem *brighten = [[FUFilterItem alloc] init];
    brighten.type = FUFilterItemTypeBrighten;
    brighten.funcName = @"color_level";
    brighten.minValue = 0;
    brighten.maxValue = 2.0;
    brighten.defaultValue = 0.2;
    brighten.value = brighten.defaultValue;
    
    FUFilterItem *thinning = [[FUFilterItem alloc] init];
    thinning.type = FUFilterItemTypeThinning;
    thinning.funcName = @"cheek_thinning";
    thinning.minValue = 0;
    thinning.maxValue = 1.0;
    thinning.defaultValue = 0;
    thinning.value = thinning.defaultValue;
    
    FUFilterItem *eye = [[FUFilterItem alloc] init];
    eye.type = FUFilterItemTypeEye;
    eye.funcName = @"eye_enlarging";
    eye.minValue = 0;
    eye.maxValue = 1.0;
    eye.defaultValue = 0.5;
    eye.value = eye.defaultValue;
    
    self.filterItems = @[smooth, brighten, thinning, eye];
    
    __weak typeof(self) weakSelf = self;
    
    dispatch_async(_asyncLoadQueue, ^{
        [weakSelf loadAllAIModel:nil];
    });
}

- (BOOL)checkLoadAIModel:(NSError **)error {
    if (!self.loadAISuccess) {
        [FURenderer releaseAIModel:FUAITYPE_FACEPROCESSOR
         | FUAITYPE_HUMANPOSE2D
         | FUAITYPE_HAIRSEGMENTATION
         | FUAITYPE_HANDGESTURE
         | FUAITYPE_FACELANDMARKS209
         | FUAITYPE_BACKGROUNDSEGMENTATION];
        BOOL isSuccess = [self loadAllAIModel:error];
        return isSuccess;
    }
    return self.loadAISuccess;
}

- (BOOL)loadAllAIModel:(NSError **)error {
    BOOL result = [self loadAIModel:@"ai_bgseg.bundle"
                 type:FUAITYPE_BACKGROUNDSEGMENTATION
            withError:error];
    
    if (!result) {
        self.loadAISuccess = false;
        return result;
    }
    
    result = [self loadAIModel:@"ai_gesture.bundle"
                          type:FUAITYPE_HANDGESTURE
                     withError:error];
    
    if (!result) {
        self.loadAISuccess = false;
        return result;
    }
    
    result = [self loadAIModel:@"ai_hairseg.bundle"
                          type:FUAITYPE_HAIRSEGMENTATION
                     withError:error];
    
    if (!result) {
        self.loadAISuccess = false;
        return result;
    }
    
    result = [self loadAIModel:@"ai_humanpose.bundle"
                          type:FUAITYPE_HUMANPOSE2D
                     withError:error];
    
    if (!result) {
        self.loadAISuccess = false;
        return result;
    }
    
    result = [self loadAIModel:@"ai_face_processor.bundle"
                          type:FUAITYPE_FACEPROCESSOR
                     withError:error];
    
    if (!result) {
        self.loadAISuccess = false;
        return result;
    }
    
    self.loadAISuccess = true;
    return self.loadAISuccess;
}

- (BOOL)loadAIModel:(NSString *)model type:(FUAITYPE)type withError:(NSError **)error {
    NSData *data = [self getModelDataWithResourceName:model];
    int result = [FURenderer loadAIModelFromPackage:(void *)data.bytes
                                               size:(int)data.length
                                             aitype:type];
    if (result <= 0) {
        if (error) {
            *error = [NSError errorWithDomain:[NSString stringWithFormat:@"FU-load-AI-%@", model]
                                         code:result
                                     userInfo:nil];
        }
        return false;
    }
    
    return true;
}

- (int)loadModel:(NSString *)model indexOfItems:(int *)index {
    NSString *path = [[NSBundle mainBundle] pathForResource:model ofType:nil];
    int itemHandle = [FURenderer itemWithContentsOfFile:path];
    int itemsIndex = itemsCount;
    *index = itemsIndex;
    items[itemsIndex] = itemHandle;
    itemsCount ++;
    return itemHandle;
}

- (NSData *)getModelDataWithResourceName:(NSString *)resource {
    return [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource:resource ofType:nil]];
}

@end
