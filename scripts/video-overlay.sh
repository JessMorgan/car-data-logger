#!/bin/sh
pushd ../app/video-overlay
java -cp ../../release/target/release-0.1.0-SNAPSHOT/app/video-overlay-0.1.0-SNAPSHOT.jar:../../release/target/release-0.1.0-SNAPSHOT/lib/video-overlay-gauge-api-0.1.0-SNAPSHOT.jar:../../release/target/release-0.1.0-SNAPSHOT/lib/plugin-0.1.0-SNAPSHOT.jar:../../parent/lib/jspf.core-1.0.2.jar jess.morgan.car_data_logger.video_overlay.Application "$@"
popd
