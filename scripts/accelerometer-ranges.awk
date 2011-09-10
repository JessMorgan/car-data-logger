#!/usr/bin/awk -f
BEGIN {
	FS = ",";
}
{
	if(NR == 1) {
		xmin = $1;
		xmax = xmin;
		ymin = $2;
		ymax = ymin;
		zmin = $3;
		zmax = zmin;
	}

	if($1 < xmin) {
		xmin = $1;
	}
	if($1 > xmax) {
		xmax = $1;
	}
	if($2 < ymin) {
		ymin = $2;
	}
	if($2 > ymax) {
		ymax = $2;
	}
	if($3 < zmin) {
		zmin = $3;
	}
	if($3 > zmax) {
		zmax = $3;
	}
}
END {
	print "x:",xmin,"-",xmax;
	print "y:",ymin,"-",ymax;
	print "z:",zmin,"-",zmax;
}
