// This is a test harness for your module
// You should do something interesting in this harness
// to test out the module and to provide instructions
// to users on how to use it by example.

// open a single window
var win = Ti.UI.createWindow({
    backgroundColor : 'white',
    layout : 'vertical',
    modal:true
});

// win.orientationModes = [Ti.UI.PORTRAIT];
var accelerometerAdded = false;
var infoLabel = Ti.UI.createLabel({
    text : '  ',
    color : '#000',
    font : {
        fontSize : 18,
        fontWeight : 'bold'
    },
    width : Ti.UI.FILL,
    left : '5'
});
win.add(infoLabel);

var orientationLabel = Ti.UI.createLabel({
    text : '  ',
    color : '#000',
    font : {
        fontSize : 18,
        fontWeight : 'bold'
    },
    width : Ti.UI.FILL,
    left : '5'
});
win.add(orientationLabel);

var compassTestLabel = Ti.UI.createLabel({
    text : '^',
    color : '#000',
    font : {
        fontSize : 140,
        fontWeight : 'bold'
    }
});
win.add(compassTestLabel);

var ax = Titanium.UI.createLabel({
    text : 'x:',
    left : 10,
    font : {
        fontSize : 14
    },
    color : '#555',
    width : 300,
    height : 'auto'
});
win.add(ax);

var ay = Titanium.UI.createLabel({
    text : 'y:',
    left : 10,
    font : {
        fontSize : 14
    },
    color : '#555',
    width : 300,
    height : 'auto'
});
win.add(ay);

var az = Titanium.UI.createLabel({
    text : 'z:',
    left : 10,
    font : {
        fontSize : 14
    },
    color : '#555',
    width : 300,
    height : 'auto'
});
win.add(az);

var velocityLabel = Titanium.UI.createLabel({
    text : 'velocity:',
    left : 10,
    font : {
        fontSize : 14
    },
    color : '#555',
    width : 300,
    height : 'auto'
});
win.add(velocityLabel);

var ts = Titanium.UI.createLabel({
    text : 'timestamp:',
    left : 10,
    font : {
        fontSize : 14
    },
    color : '#555',
    width : 300,
    height : 'auto'
});
win.add(ts);

win.open();

// TODO: write your module tests here
var akylas_motion_android = require('akylas.motion');
Ti.API.info("module is => " + akylas_motion_android);

var accelX = 0, accelY = 0, accelZ = 0;
var motionWasRegistered = false;

var magnetometerCallback = function(e) {
    // labels['mag.x'].updatePositionData(e.x);
    // labels['mag.y'].updatePositionData(e.y);
    // labels['mag.z'].updatePositionData(e.z);
};

var currentYaw = null, currentPitch = null, currentRoll = 0;
var maxVelocity = 0;
var lastTimestamp = -1;
var gravX = 0, gravY = 0, gravZ = 0, prevVelocity = 0, prevAcce = 0;
var kFilteringFactor = 0.1;

function tendToZero(value) {
    if (value < 0) {
        return Math.ceil(value);
    } else {
        return Math.floor(value);
    }
}

var resetVelocityValue = 40;
var resetVelocityDuration = 400;
var resetYaw = 0;
var resetPitch = 0;
var resetRoll = 0;

var resetTimer = null;
var currentTimestamp = 0;

function computeVelocity(_acc) {
    if (lastTimestamp === -1) {
        lastTimestamp = currentTimestamp;
        return;
    }

    var deltaTime = (currentTimestamp - lastTimestamp) / 1000;
    if(deltaTime <  0) return; //we are not always sure of the order. it seems!
    //in seconds

    var vector = Math.sqrt(Math.pow(_acc.x, 2) + Math.pow(_acc.y, 2) + Math.pow(_acc.z, 2));
    var acce = vector - prevVelocity;
    var velocity = (((acce - prevAcce) / 2) * deltaTime) + prevVelocity;
    var realVel = velocity *1000;
    velocityLabel.text = 'velocity:' + realVel;
    // Ti.API.info("vector: "+vector);
    // Ti.API.info("prevVelocity: "+prevVelocity);
    // Ti.API.info("velocity: "+velocity);
    Ti.API.info("deltaTime: " + deltaTime);
    // Ti.API.info("prevAcce: "+prevAcce);
    // Ti.API.info("acce: "+acce);
    if (realVel < resetVelocityValue) {
        // Ti.API.info("velocity under reset value: " + realVel);

        if (maxVelocity > resetVelocityValue && resetTimer === null) {
            var yawDelta = currentYaw - resetYaw;
            var newMax = maxVelocity;
            resetTimer = setTimeout(function() {
                infoLabel.text = 'LastMax: ' + newMax.toFixed(2);
                if (yawDelta < -0.4)
                    velocityLabel.text += '      Right';
                else if (yawDelta > 0.4)
                    velocityLabel.text += '      Left';
                else
                    velocityLabel.text += '      Center';
                maxVelocity = 0;

            }, resetVelocityDuration);
        } else {
            infoLabel.text = 'Reseting values';
        }
        resetYaw = currentYaw;
        resetPitch = currentPitch;
        resetRoll = currentRoll;
    } else {
        Ti.API.info("Velocity: " + realVel);
        if (resetTimer !== null) {
            clearTimeout(resetTimer);
            resetTimer = null;
        }
        if (realVel > maxVelocity) {
            Ti.API.info("New Max Velocity: " + realVel);
            maxVelocity = realVel;
        }
    }

    prevAcce = acce;
    prevVelocity = velocity;
    lastTimestamp = currentTimestamp;
}

//
// EVENT LISTENER FOR COMPASS EVENTS - THIS WILL FIRE REPEATEDLY (BASED ON HEADING FILTER)
//
var headingCallback = function(e) {
    if (e.error) {
        compassTestLabel.text = 'error: ' + e.error;
        compassTestLabel.transform = Ti.UI.create2DMatrix();
        Ti.API.info("Code translation: " + translateErrorCode(e.code));
        return;
    }
    var degreesAngle = -e.heading.magneticHeading;
    compassTestLabel.transform = Ti.UI.create2DMatrix({
        rotate : degreesAngle
    });
};

var motionCallback = function(e) {
    currentTimestamp = e.timestamp;
    currentYaw = e.orientation.yaw;
    currentPitch = e.orientation.pitch;
    currentRoll = e.orientation.roll;
    computeVelocity(e.accelerometer.user);

    // Ti.API.info('motion ' + JSON.stringify(e));
    ts.text = e.timestamp;
    ax.text = 'x: ' + e.accelerometer.user.x;
    ay.text = 'y:' + e.accelerometer.user.y;
    az.text = 'z:' + e.accelerometer.user.z;
};
// var heading = Math.atan2(e.rotationMatrix.m22, e.rotationMatrix.m12);
// heading = heading*180/Math.PI;
// Ti.API.info('heading ' + heading);
// compassTestLabel.transform = Ti.UI.create2DMatrix({rotate:heading})
// }

win.addEventListener('close', function() {
    // Ti.Motion.removeEventListener('accelerometer', accelerometerCallback);
    // Ti.Motion.removeEventListener('magnetometer', magnetometerCallback);
    // Ti.Motion.removeEventListener('orientation', orientationCallback);
    // Ti.Motion.removeEventListener('gyroscope', gyroscopeCallback);
    akylas_motion_android.removeEventListener('motion', motionCallback);
    Titanium.Geolocation.removeEventListener('heading', headingCallback);
    motionWasRegistered = false;
});

win.addEventListener('android:back', function(e) {
    this.close();
});
 
// Ti.Motion.addEventListener('accelerometer', accelerometerCallback);
// Ti.Motion.addEventListener('magnetometer', magnetometerCallback);
// Ti.Motion.addEventListener('orientation', orientationCallback);
akylas_motion_android.addEventListener('motion', motionCallback);
motionWasRegistered = true;
//
// IF WE HAVE COMPASS GET THE HEADING
//
if (Titanium.Geolocation.hasCompass) {
    //
    //  TURN OFF ANNOYING COMPASS INTERFERENCE MESSAGE
    //
    Titanium.Geolocation.showCalibration = false;

    //
    // SET THE HEADING FILTER (THIS IS IN DEGREES OF ANGLE CHANGE)
    // EVENT WON'T FIRE UNLESS ANGLE CHANGE EXCEEDS THIS VALUE
    Titanium.Geolocation.headingFilter = 2;

    //
    //  GET CURRENT HEADING - THIS FIRES ONCE
    //
    Ti.Geolocation.getCurrentHeading(function(e) {
        headingCallback(e);
    });
    // Titanium.Geolocation.addEventListener('heading', headingCallback);
}

if (Titanium.Platform.name == 'iPhone OS' && Titanium.Platform.model == 'Simulator') {
    var notice = Titanium.UI.createLabel({
        bottom : 50,
        font : {
            fontSize : 18
        },
        color : '#900',
        width : 'auto',
        text : 'Note: Accelerometer does not work in simulator',
        textAlign : 'center'
    });
    win.add(notice);
}

// if (Titanium.Platform.name == 'android')
// {
Ti.App.addEventListener('pause', function(e) {
    Ti.API.info("removing motion callbacks on pause");
    // Ti.Motion.removeEventListener('accelerometer', accelerometerCallback);
    // Ti.Motion.removeEventListener('magnetometer', magnetometerCallback);
    // Ti.Motion.removeEventListener('orientation', orientationCallback);
    if (motionWasRegistered)
        akylas_motion_android.removeEventListener('motion', motionCallback);
    // Titanium.Geolocation.removeEventListener('heading', headingCallback);
});
Ti.App.addEventListener('paused', function(e) {
    Ti.API.info("removing motion callbacks on paused");
    // Ti.Motion.removeEventListener('accelerometer', accelerometerCallback);
    // Ti.Motion.removeEventListener('magnetometer', magnetometerCallback);
    // Ti.Motion.removeEventListener('orientation', orientationCallback);
    if (motionWasRegistered)
        akylas_motion_android.removeEventListener('motion', motionCallback);
    // Titanium.Geolocation.removeEventListener('heading', headingCallback);
});
Ti.App.addEventListener('resume', function(e) {
    Ti.API.info("adding motion callbacks on resume");
    // Ti.Motion.addEventListener('accelerometer', accelerometerCallback);
    // Ti.Motion.addEventListener('magnetometer', magnetometerCallback);
    // Ti.Motion.addEventListener('orientation', orientationCallback);
    if (motionWasRegistered)
        akylas_motion_android.addEventListener('motion', motionCallback);
    // Titanium.Geolocation.addEventListener('heading', headingCallback);
});
// }

