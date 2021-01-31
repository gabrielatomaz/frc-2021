package frc.robot.subsystems;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Units;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.core.components.SmartNavX;
import frc.robot.Constants.DrivetrainConstants;

public class Drivetrain extends SubsystemBase {
  private final SpeedControllerGroup motorsRight, motorsLeft;
  private final DifferentialDrive drive;
  private final Encoder encoderRight, encoderLeft;
  private final SmartNavX navX; 
  private final DifferentialDriveOdometry odometry;

  public Drivetrain() {
    this.motorsRight = new SpeedControllerGroup(
      new VictorSP(DrivetrainConstants.motorRightPort[0]), 
      new VictorSP(DrivetrainConstants.motorRightPort[1])
    );
    this.motorsLeft = new SpeedControllerGroup(
      new VictorSP(DrivetrainConstants.motorLeftPort[0]), 
      new VictorSP(DrivetrainConstants.motorLeftPort[1])
    );

    this.drive = new DifferentialDrive(this.motorsRight, this.motorsLeft);

    this.encoderLeft = new Encoder(
      DrivetrainConstants.encoderLeftPort[0],
      DrivetrainConstants.encoderLeftPort[1]
    );
 
    this.encoderRight = new Encoder(
      DrivetrainConstants.encoderRightPort[0],
      DrivetrainConstants.encoderRightPort[1],
      DrivetrainConstants.isEncoderRightInverted
    );

    this.navX = new SmartNavX(); 

    this.odometry = new DifferentialDriveOdometry(this.navX.getRotation2d());

    this.setEncodersDistancePerPulse();
    this.resetEncoders();
  }

  public void arcadeDrive(double forward, double rotation) {
    this.drive.arcadeDrive(forward, rotation);
  }

  public void resetEncoders() {
    this.encoderLeft.reset();
    this.encoderRight.reset();
  }

  public void setEncodersDistancePerPulse() {
    var wheelCircumferenceMeters = Units.inchesToMeters(DrivetrainConstants.wheelRadius) * 2 * Math.PI;

    var distancePerPulseLeft = wheelCircumferenceMeters / (double) DrivetrainConstants.pulsesLeft;
    var distancePerPulseRight = wheelCircumferenceMeters / (double) DrivetrainConstants.pulsesRight;

    this.encoderLeft.setDistancePerPulse(distancePerPulseLeft);
    this.encoderRight.setDistancePerPulse(distancePerPulseRight);
  }

  public double getAverageDistance() {
    var averageDistance = (this.encoderLeft.getDistance() + this.encoderRight.getDistance()) / 2.0;
    
    return averageDistance;
  }

  public double getAngle() {
    return this.navX.getAngle();
  }

  public void reset() {
    this.navX.reset();
  }

  public void updateOdometry() {
    this.odometry.update(this.navX.getRotation2d(), this.encoderLeft.getDistance(), this.encoderRight.getDistance());
  }

  public Pose2d getPose() {
    return this.odometry.getPoseMeters();
  }

  public DifferentialDriveWheelSpeeds getWheelSpeeds() {
    var driveWheelSpeeds = new DifferentialDriveWheelSpeeds(this.encoderLeft.getRate(), this.encoderRight.getRate());
    
    return driveWheelSpeeds;
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Encoder left", encoderLeft.get());
    SmartDashboard.putNumber("Encoder right", encoderRight.get());
    SmartDashboard.putNumber("Average distance", this.getAverageDistance());
    
    this.updateOdometry();
  }
}
