//
//  CameraView.h
//  OpenCViOS
//
//  Created by Karasawa on 2014/01/13.
//
//

#import <UIKit/UIKit.h>

using namespace std;
using namespace cv;

@interface CameraView : UIImageView<CvVideoCameraDelegate>{
@private
    CvVideoCamera* m_videoCamera;

    float THRESHOLD; //閾値
    ORB* m_detector; //ORB特徴点検出器
//    DescriptorExtractor* m_extractor; //ORB特徴量抽出機
    BFMatcher* m_matcher; //特徴量照合器

    id m_callbackInstance;
    SEL m_callbackSelector;
}

@property (nonatomic, retain) CvVideoCamera* m_videoCamera;

- (void)startDetect:(id)callbackInstance selector:(SEL)callbackSelector;
- (void)stopDetect;
- (void) setTrainingImages:(Mat)mrgba;
- (int) detectImage:(Mat)mrgba;

@end
