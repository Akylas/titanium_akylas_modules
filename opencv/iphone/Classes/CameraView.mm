//
//  CameraView.m
//  OpenCViOS
//
//  Created by Karasawa on 2014/01/13.
//
//

#import "CameraView.hpp"

@implementation CameraView

@synthesize m_videoCamera;

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        self.m_videoCamera = [[CvVideoCamera alloc] initWithParentView:self];
        self.m_videoCamera.defaultAVCaptureDevicePosition = AVCaptureDevicePositionBack;
        self.m_videoCamera.defaultAVCaptureSessionPreset = AVCaptureSessionPreset640x480;
        self.m_videoCamera.defaultAVCaptureVideoOrientation = AVCaptureVideoOrientationPortrait;
        self.m_videoCamera.defaultFPS = 30;
        self.m_videoCamera.delegate = self;
        
        m_callbackInstance = nil;
        m_callbackSelector = nil;

        THRESHOLD = 45; //閾値
        m_matcher = new BFMatcher(NORM_HAMMING, true);
        m_detector = ORB::create(300); //ORB特徴点検出器
//        m_extractor = new OrbDescriptorExtractor; //ORB特徴量抽出機
    }
    return self;
}

-(void)dealloc
{
    delete m_detector;
//    delete m_extractor;
    delete m_matcher;
    [super dealloc];
}

- (void)startDetect:(id)callbackInstance selector:(SEL)callbackSelector
{
    m_callbackInstance = callbackInstance;
    m_callbackSelector = callbackSelector;
    [self.m_videoCamera start];
}

- (void)stopDetect
{
    [self.m_videoCamera stop];
    m_callbackInstance = nil;
    m_callbackSelector = nil;
}

#pragma mark - Protocol CvVideoCameraDelegate

#ifdef __cplusplus
- (void)processImage:(Mat&)image;
{
    int similarity = [self detectImage:image];
    if ( m_callbackInstance != nil && m_callbackSelector != nil ){
        NSNumber* num = [[NSNumber alloc] initWithInt:similarity];
        [m_callbackInstance performSelector:m_callbackSelector withObject:(id)num];
    }
}

- (void)setTrainingImages:(Mat)mrgba
{
    vector<Mat> trainDescriptorses;
    vector<KeyPoint>trainKeypoints;
    Mat trainDescriptors;
        
    Mat gray(mrgba.cols, mrgba.rows, CV_8UC1);
    cvtColor(mrgba, gray, CV_RGBA2GRAY, 0);//グレースケールへ変換
    m_detector->detect(gray, trainKeypoints);// 特徴点をtrainKeypointsへ格納
    m_detector->compute(gray, trainKeypoints, trainDescriptors);//各特徴点の特徴ベクトルをtrainDescriptorsへ格納
    trainDescriptorses.push_back(trainDescriptors);
    m_matcher->add(trainDescriptorses); //照合器へ全ての学習画像の特徴ベクトルを登録
}
    
- (int)detectImage:(Mat)mrgba
{
    vector < KeyPoint > queryKeypoints;
        Mat queryDescriptors;
        
    Mat mgray(mrgba.cols, mrgba.rows, CV_8UC1);
    cvtColor(mrgba, mgray, CV_RGBA2GRAY, 0);//グレースケールへ変換
    
    m_detector->detect(mgray, queryKeypoints);
    m_detector->compute(mgray, queryKeypoints, queryDescriptors);
        
    // BrustForceMatcher による画像マッチング
    vector < DMatch > matches;
    //m_matcher->match(queryDescriptors, matches);
        
    int votes[1]; // 学習画像の投票箱
    for (int i = 0; i < 1; i++)
        votes[i] = 0;
        
    // キャプチャ画像の各特徴点に対して、ハミング距離が閾値より小さい特徴点を持つ学習画像へ投票
    for (int i = 0; i < matches.size(); i++) {
        if (matches[i].distance < THRESHOLD) {
            votes[matches[i].imgIdx]++;
        }
    }
        
    // 投票数の多い画像のIDを調査
    int maxImageId = -1;
    int maxVotes = 0;
    for (int i = 0; i < 1; i++) {
        if (votes[i] > maxVotes) {
            maxImageId = i;  //マッチした特徴点を一番多く持つ学習画像のID
            maxVotes = votes[i]; //マッチした特徴点の数
        }
    }
        
    vector < Mat > trainDescs = m_matcher->getTrainDescriptors();
        
    float similarity = (float) maxVotes / trainDescs[maxImageId].rows * 100;
    if (similarity < 5) {
        maxImageId = -1; // マッチした特徴点の数が全体の5%より少なければ、未検出とする
    }
        
    //	return maxImageId;
    return (int)similarity;
}

#endif

@end
