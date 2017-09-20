#import "NTLicenseManagerListener.h"
#import "NTAssetTileDataSource.h"
#import "NTBitmapOverlayRasterTileDataSource.h"
#import "NTCacheTileDataSource.h"
#import "NTCartoOnlineTileDataSource.h"
#import "NTCombinedTileDataSource.h"
#import "NTGDALRasterTileDataSource.h"
#import "NTHTTPTileDataSource.h"
#import "NTLocalVectorDataSource.h"
#import "NTMBTilesTileDataSource.h"
#import "NTMemoryCacheTileDataSource.h"
#import "NTNMLModelLODTreeDataSource.h"
#import "NTOfflineNMLModelLODTreeDataSource.h"
#import "NTOGRVectorDataBase.h"
#import "NTOGRVectorDataSource.h"
#import "NTOnlineNMLModelLODTreeDataSource.h"
#import "NTPackageManagerTileDataSource.h"
#import "NTPersistentCacheTileDataSource.h"
#import "NTTileDataSource.h"
#import "NTVectorDataSource.h"
#import "NTDouglasPeuckerGeometrySimplifier.h"
#import "NTGeometry.h"
#import "NTGeometrySimplifier.h"
#import "NTLineGeometry.h"
#import "NTMultiGeometry.h"
#import "NTMultiLineGeometry.h"
#import "NTMultiPointGeometry.h"
#import "NTMultiPolygonGeometry.h"
#import "NTPointGeometry.h"
#import "NTPolygonGeometry.h"
#import "NTCartoOfflineVectorTileLayer.h"
#import "NTCartoOnlineRasterTileLayer.h"
#import "NTCartoOnlineVectorTileLayer.h"
#import "NTCartoVectorTileLayer.h"
#import "NTClusteredVectorLayer.h"
#import "NTClusterElementBuilder.h"
#import "NTEditableVectorLayer.h"
#import "NTLayer.h"
#import "NTNMLModelLODTreeEventListener.h"
#import "NTNMLModelLODTreeLayer.h"
#import "NTRasterTileLayer.h"
#import "NTSolidLayer.h"
#import "NTTileLayer.h"
#import "NTTileLoadListener.h"
#import "NTTorqueTileLayer.h"
#import "NTUTFGridEventListener.h"
#import "NTVectorEditEventListener.h"
#import "NTVectorElementEventListener.h"
#import "NTVectorLayer.h"
#import "NTVectorTileEventListener.h"
#import "NTVectorTileLayer.h"
#import "NTCartoPackageManager.h"
#import "NTPackageManager.h"
#import "NTPackageManagerListener.h"
#import "NTEPSG3857.h"
#import "NTProjection.h"
#import "NTMapRendererListener.h"
#import "NTRedrawRequestListener.h"
#import "NTRendererCaptureListener.h"
#import "NTCartoOnlineRoutingService.h"
#import "NTOSRMOfflineRoutingService.h"
#import "NTPackageManagerRoutingService.h"
#import "NTRoutingService.h"
#import "NTCartoVisBuilder.h"
#import "NTBalloonPopupStyle.h"
#import "NTBalloonPopupStyleBuilder.h"
#import "NTBillboardStyle.h"
#import "NTBillboardStyleBuilder.h"
#import "NTGeometryCollectionStyle.h"
#import "NTGeometryCollectionStyleBuilder.h"
#import "NTLabelStyle.h"
#import "NTLabelStyleBuilder.h"
#import "NTLineStyle.h"
#import "NTLineStyleBuilder.h"
#import "NTMarkerStyle.h"
#import "NTMarkerStyleBuilder.h"
#import "NTPointStyle.h"
#import "NTPointStyleBuilder.h"
#import "NTPolygon3DStyle.h"
#import "NTPolygon3DStyleBuilder.h"
#import "NTPolygonStyle.h"
#import "NTPolygonStyleBuilder.h"
#import "NTPopupStyle.h"
#import "NTPopupStyleBuilder.h"
#import "NTStyle.h"
#import "NTStyleBuilder.h"
#import "NTTextStyle.h"
#import "NTTextStyleBuilder.h"
#import "NTMapEventListener.h"
#import "NTAssetPackage.h"
#import "NTLogEventListener.h"
#import "NTZippedAssetPackage.h"
#import "NTBalloonPopup.h"
#import "NTBillboard.h"
#import "NTCustomPopup.h"
#import "NTCustomPopupHandler.h"
#import "NTGeometryCollection.h"
#import "NTLabel.h"
#import "NTLine.h"
#import "NTMarker.h"
#import "NTNMLModel.h"
#import "NTPoint.h"
#import "NTPolygon.h"
#import "NTPolygon3D.h"
#import "NTPopup.h"
#import "NTText.h"
#import "NTVectorElement.h"
#import "NTCartoVectorTileDecoder.h"
#import "NTMBVectorTileDecoder.h"
#import "NTTorqueTileDecoder.h"
#import "NTVectorTileDecoder.h"
static void initNTPolymorphicClasses() {
  [NTLicenseManagerListener class];
  [NTAssetTileDataSource class];
  [NTBitmapOverlayRasterTileDataSource class];
  [NTCacheTileDataSource class];
  [NTCartoOnlineTileDataSource class];
  [NTCombinedTileDataSource class];
  [NTHTTPTileDataSource class];
  [NTLocalVectorDataSource class];
  [NTMBTilesTileDataSource class];
  [NTMemoryCacheTileDataSource class];
  [NTPackageManagerTileDataSource class];
  [NTPersistentCacheTileDataSource class];
  [NTTileDataSource class];
  [NTVectorDataSource class];
  [NTDouglasPeuckerGeometrySimplifier class];
  [NTGeometry class];
  [NTGeometrySimplifier class];
  [NTLineGeometry class];
  [NTMultiGeometry class];
  [NTMultiLineGeometry class];
  [NTMultiPointGeometry class];
  [NTMultiPolygonGeometry class];
  [NTPointGeometry class];
  [NTPolygonGeometry class];
  [NTCartoOfflineVectorTileLayer class];
  [NTCartoOnlineRasterTileLayer class];
  [NTCartoOnlineVectorTileLayer class];
  [NTCartoVectorTileLayer class];
  [NTClusteredVectorLayer class];
  [NTClusterElementBuilder class];
  [NTEditableVectorLayer class];
  [NTLayer class];
  [NTRasterTileLayer class];
  [NTSolidLayer class];
  [NTTileLayer class];
  [NTTileLoadListener class];
  [NTTorqueTileLayer class];
  [NTUTFGridEventListener class];
  [NTVectorEditEventListener class];
  [NTVectorElementEventListener class];
  [NTVectorLayer class];
  [NTVectorTileEventListener class];
  [NTVectorTileLayer class];
  [NTCartoPackageManager class];
  [NTPackageManager class];
  [NTPackageManagerListener class];
  [NTEPSG3857 class];
  [NTProjection class];
  [NTMapRendererListener class];
  [NTRedrawRequestListener class];
  [NTRendererCaptureListener class];
  [NTCartoOnlineRoutingService class];
  [NTOSRMOfflineRoutingService class];
  [NTPackageManagerRoutingService class];
  [NTRoutingService class];
  [NTCartoVisBuilder class];
  [NTBalloonPopupStyle class];
  [NTBalloonPopupStyleBuilder class];
  [NTBillboardStyle class];
  [NTBillboardStyleBuilder class];
  [NTGeometryCollectionStyle class];
  [NTGeometryCollectionStyleBuilder class];
  [NTLabelStyle class];
  [NTLabelStyleBuilder class];
  [NTLineStyle class];
  [NTLineStyleBuilder class];
  [NTMarkerStyle class];
  [NTMarkerStyleBuilder class];
  [NTPointStyle class];
  [NTPointStyleBuilder class];
  [NTPolygon3DStyle class];
  [NTPolygon3DStyleBuilder class];
  [NTPolygonStyle class];
  [NTPolygonStyleBuilder class];
  [NTPopupStyle class];
  [NTPopupStyleBuilder class];
  [NTStyle class];
  [NTStyleBuilder class];
  [NTTextStyle class];
  [NTTextStyleBuilder class];
  [NTMapEventListener class];
  [NTAssetPackage class];
  [NTLogEventListener class];
  [NTZippedAssetPackage class];
  [NTBalloonPopup class];
  [NTBillboard class];
  [NTCustomPopup class];
  [NTCustomPopupHandler class];
  [NTGeometryCollection class];
  [NTLabel class];
  [NTLine class];
  [NTMarker class];
  [NTNMLModel class];
  [NTPoint class];
  [NTPolygon class];
  [NTPolygon3D class];
  [NTPopup class];
  [NTText class];
  [NTVectorElement class];
  [NTCartoVectorTileDecoder class];
  [NTMBVectorTileDecoder class];
  [NTTorqueTileDecoder class];
  [NTVectorTileDecoder class];
}
