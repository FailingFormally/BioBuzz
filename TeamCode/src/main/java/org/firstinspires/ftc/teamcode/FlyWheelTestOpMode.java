// Made by failing formally for FTC Biobuzz 2026/2027. Uses code from FTC with a non open source license
//that we distributed legally, our code is open source but please read.

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

/**
 * This OpMode can be used to test the optimal velocity for the FlyWheel launcher.
 * Use DPAD_UP and DPAD_DOWN to adjust the velocity.  The Target and actual velocities
 * are printed to the screen.  Test shooting at different velocities to find the best
 * velocity for different ranges.
 */
@TeleOp(name="Test Flywheel Velocities")
public class FlyWheelTestOpMode extends OpMode {

    /**
     * When comparing doubles, you won't get exact matches.  For example, 1000 < 1000.1.
     * Use this tolerance to adjust the comparisons.
     */
    final double tolerance = 0.1;

    private DcMotorEx flyWheel;

    // make intake variable here and uncomment code once done

    /**
     * Tracking the state of DPAD UP and DOWN
     */
    boolean wasUp, wasDown;

    /*
     * When we control our launcher motor, we are using encoders. These allow the control system
     * to read the current speed of the motor and apply more or less power to keep it at a constant
     * velocity. Here we are setting the target, and minimum velocity that the launcher should run
     * at. The minimum velocity is a threshold for determining when to fire.
     */
    private double targetVelocity = 1000.0;

    /**
     * This enum is called the "LaunchState". It reflects the current condition of the shooter
     * motor and we move through the enum when the user asks our code to fire a shot.
     * It starts at idle, when the user requests a launch, we enter SPIN_UP where we get the
     * motor up to speed, once it meets a minimum speed then it starts and then ends the launch process.
     * We can use higher level code to cycle through these states. But this allows us to write
     * functions and autonomous routines in a way that avoids loops within loops, and "waits".
     */
    private enum LaunchState {
        /**
         * The default state
         */
        IDLE,
        /**
         * Once the launch is triggered, we stay in the SPIN_UP state until the
         * motors are running at the target velocity.
         */
        SPIN_UP,
        /**
         * Once motors hit target velocity, we hit this state so that we can trigger the launch.
         * Ideally we would trigger servos to put the artifact in place here.
         */
        LAUNCH,
        /**
         * In this state, we are actively waiting for the launch to occur.
         * From here, we probably need to return to SPIN_UP to get back to launch
         * velocity.
         */
        LAUNCHING
    }

    private LaunchState launchState = LaunchState.IDLE;

    /**
     * User-defined init method
     * <p>
     * This method will be called once, when the INIT button is pressed.
     */
    @Override
    public void init() {
        flyWheel = hardwareMap.get(DcMotorEx.class, "flywheel");
//        intake = hardwareMap.get(DcMotorEx.class, "intake");
        /*
         * Here we set our launcher to the RUN_USING_ENCODER runmode.
         * If you notice that you have no control over the velocity of the motor, it just jumps
         * right to a number much higher than your set point, make sure that your encoders are plugged
         * into the port right beside the motor itself. And that the motors polarity is consistent
         * through any wiring.
         */
        flyWheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
//        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        PIDFCoefficients coefficients = new PIDFCoefficients(300, 0, 0, 10);
        flyWheel.setPIDFCoefficients(DcMotorEx.RunMode.RUN_USING_ENCODER, coefficients);

        /*
         * Tell the driver that initialization is complete.
         */
        telemetry.addData("Status", "Initialized");
    }

    /**
     * User-defined loop method
     * <p>
     * This method will be called repeatedly during the period between when
     * the play button is pressed and when the OpMode is stopped.
     */
    @Override
    public void loop() {
        // Increase the target angular velocity by 100
        if (gamepad1.dpad_up && !wasUp) {
            targetVelocity += 100;
        }
        wasUp = gamepad1.dpad_up;

        // Decrease the target angular velocity by 100
        if (gamepad1.dpad_down && !wasDown) {
            targetVelocity -= 100;
        }
        wasDown = gamepad1.dpad_down;

//        if (gamepad1.left_bumper) {
//            intake.setPower(1.0);
//        }

//        if (gamepad1.x) {
//            intake.setPower(0);
//        }
//
//        if (gamepad1.right_bumper) {
//            intake.setPower(-1);
//        }

        telemetry.addData("Target Velocity", targetVelocity);
        telemetry.addData("Velocity", flyWheel.getVelocity());
        telemetry.addData("Launch State", launchState.toString());

        launch(gamepad1.yWasPressed(), targetVelocity);

        telemetry.update();

    }

    void launch(boolean shotRequested, double shootVelocity) {
        switch (launchState) {
            case IDLE:
                if (shotRequested) {
                    launchState = LaunchState.SPIN_UP;
                }
                break;
            case SPIN_UP:
                flyWheel.setVelocity(shootVelocity);
                // Advance to LAUNCH when both motors are up to speed
                if (flyWheel.getVelocity() >= (shootVelocity - tolerance)) {
                    launchState = LaunchState.LAUNCH;
                }
                break;
            case LAUNCH:
                flyWheel.setVelocity(shootVelocity);
                // For now, if velocity drops on either flyWheel, we probably fired...
                // go back to SPIN_UP
                if (flyWheel.getVelocity() < (shootVelocity - tolerance)) {
                    launchState = LaunchState.SPIN_UP;
                }
                break;
        }
    }

}
