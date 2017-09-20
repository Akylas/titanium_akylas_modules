/**
 * akylas.opencv
 *
 * Created by Your Name
 * Copyright (c) 2017 Your Company. All rights reserved.
 */

#import "AkylasOpencvModule.hpp"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TiImageHelper.h"
#import "UIImage+Alpha.h"

@implementation AkylasOpencvModule

#pragma mark Internal

// This is generated for your module, please do not change it
- (id)moduleGUID
{
	return @"94f5086d-b4e6-456d-bc97-0686f52a5577";
}

// This is generated for your module, please do not change it
- (NSString *)moduleId
{
	return @"akylas.opencv";
}

#pragma mark Lifecycle

- (void)startup
{
	// This method is called when the module is first loaded
	// You *must* call the superclass
	[super startup];
	NSLog(@"[DEBUG] %@ loaded",self);
}

#pragma Public APIs

- (id)example:(id)args
{
	// Example method. 
	// Call with "MyModule.example(args)"
	return @"hello world";
}

- (id)exampleProp
{
	// Example property getter. 
	// Call with "MyModule.exampleProp" or "MyModule.getExampleProp()"
	return @"Titanium rocks!";
}

bool stitch(const std::vector <cv::Mat> & images, cv::Mat &result) {
    cv::Stitcher stitcher = cv::Stitcher::createDefault(false);
    
    cv::Stitcher::Status status = stitcher.stitch(images, result);
    
    if (status != cv::Stitcher::OK) {
        return false;
    }
    
    return true;
}

- (id)stitchImages:(id)args
{
    ENSURE_SINGLE_ARG(args, NSArray)
    __block std::vector<cv::Mat> mats;
    [args enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        UIImage* image = [TiImageHelper convertToUIImage:args withProxy:self];
        if (image) {
            cv::Mat mat;
            UIImageToMat(image, mat);
            mats.push_back(mat);
        }
    }];
    cv::Mat result;
    stitch(mats, result);
    return [[[TiBlob alloc] initWithImage:MatToUIImage(result)] autorelease];
}
void WriteUIImageToMat(const UIImage* image, cv::Mat& m, BOOL noAlpha = YES) {
    CGColorSpaceRef colorSpace = CGImageGetColorSpace(image.CGImage);
    CGFloat cols = CGImageGetWidth(image.CGImage), rows = CGImageGetHeight(image.CGImage);
    CGContextRef contextRef;
    CGBitmapInfo bitmapInfo = kCGImageAlphaPremultipliedLast;
    if (CGColorSpaceGetModel(colorSpace) == kCGColorSpaceModelMonochrome)
    {
        m.create(rows, cols, CV_8UC1); // 8 bits per component, 1 channel
        bitmapInfo = kCGImageAlphaNone;
        if (noAlpha)
            bitmapInfo = kCGImageAlphaNone;
        else
            m = cv::Scalar(0);
        contextRef = CGBitmapContextCreate(m.data, m.cols, m.rows, 8,
                                           m.step[0], CGColorSpaceCreateDeviceGray(),
                                           bitmapInfo);
    }
    else
    {
        m.create(rows, cols, CV_8UC4); // 8 bits per component, 4 channels
        if (noAlpha)
            bitmapInfo = kCGImageAlphaNoneSkipLast |
            kCGBitmapByteOrderDefault;
        else
            m = cv::Scalar(0);
        contextRef = CGBitmapContextCreate(m.data, m.cols, m.rows, 8,
                                           m.step[0], CGColorSpaceCreateDeviceRGB(),
                                           bitmapInfo);
    }
    CGContextDrawImage(contextRef, CGRectMake(0, 0, cols, rows),
                       image.CGImage);
    CGContextRelease(contextRef);
}

- (id)concatenateImages:(id)args
{
    ENSURE_TYPE(args, NSArray)
    NSArray *images = nil;
    NSDictionary *options = nil;
    BOOL noAlpha = FALSE;
    ENSURE_ARG_AT_INDEX(images, args, 0, NSArray);
    ENSURE_ARG_OR_NIL_AT_INDEX(options, args, 1, NSDictionary);
    
    __block int width = -1;
    __block int height = -1;
    __block int nbChannels = noAlpha?3:-1;
    __block std::vector<cv::Mat> mats;
    __block NSUInteger nbColumns = 0;
    NSUInteger nbRows = [images count];
    [images enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if (width == -1 || height == -1 || nbChannels == -1) {
            UIImage* image = [TiImageHelper convertToUIImage:[obj objectAtIndex:0] withProxy:self];
            width = image.size.width;
            height = image.size.height;
            if (!noAlpha) {
                nbChannels = [UIImageAlpha hasAlpha:image] ? 4 : 3;
            }
        }
        nbColumns = MAX(nbColumns, [obj count]);
    }];
    cv::Mat grid;
    if (noAlpha) {
        cv::Mat3b grid((int)(nbRows * height), (int)(nbColumns * width), cv::Vec3b(0,0,0));
        // For each image
        [images enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idy, BOOL * _Nonnull stop) {
            [obj enumerateObjectsUsingBlock:^(TiBlob*  _Nonnull img, NSUInteger idx, BOOL * _Nonnull stop) {
                UIImage* image = [TiImageHelper convertToUIImage:img withProxy:self];
                if (image) {
                    cv::Mat mat;
                    
                    WriteUIImageToMat(image, mat, noAlpha);
                    cv::Rect roi((int)idx*width, (int)idy*height,width,height);
                    mat.copyTo(grid(roi));
                    mat.release();
                    [img releaseImage];
                }
            }];
        }];
        return [[[TiBlob alloc] initWithImage:MatToUIImage(grid)] autorelease];
    } else {
        cv::Mat4b grid((int)(nbRows * height), (int)(nbColumns * width), cv::Vec4b(0,0,0, 255));
        // For each image
        [images enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idy, BOOL * _Nonnull stop) {
            [obj enumerateObjectsUsingBlock:^(TiBlob*  _Nonnull img, NSUInteger idx, BOOL * _Nonnull stop) {
                UIImage* image = [TiImageHelper convertToUIImage:img withProxy:self];
                if (image) {
                    cv::Mat mat;
                    
                    WriteUIImageToMat(image, mat);
                    cv::Rect roi((int)idx*width, (int)idy*height,width,height);
                    mat.copyTo(grid(roi));
                    mat.release();
                    [img releaseImage];
                }
            }];
        }];
        return [[[TiBlob alloc] initWithImage:MatToUIImage(grid)] autorelease];
    }
}

@end
