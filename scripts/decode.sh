#!/bin/sh
pushd ../app/decode-app
java -cp ../../release/target/release-0.1.0-SNAPSHOT/app/decode-app-0.1.0-SNAPSHOT.jar:../../release/target/release-0.1.0-SNAPSHOT/lib/decode-api-0.1.0-SNAPSHOT.jar:../../release/target/release-0.1.0-SNAPSHOT/lib/data-processor-api-0.1.0-SNAPSHOT.jar:../../release/target/release-0.1.0-SNAPSHOT/lib/plugin-0.1.0-SNAPSHOT.jar:../../parent/lib/jspf.core-1.0.2.jar jess.morgan.car_data_logger.app.decode.Application "$@"
popd
