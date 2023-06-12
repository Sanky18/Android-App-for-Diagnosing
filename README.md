# Patient Diagnosis Tool

This is a software tool developed for diagnosing patients, specifically focusing on measuring stride length and step length to compare with Parkinson's disease patients. The tool utilizes sensor data from various sources and applies the trapezoidal rule to calculate the rotation covered by the patient's leg.

## Features

- Measures stride length and step length of patients
- Compares the results with Parkinson's disease patients
- Utilizes sensor data from accelerometer and gyroscope
- Applies the trapezoidal rule for integration
- Provides analysis and diagnosis based on the calculated measurements

## Requirements

To run this tool, you need the following software and packages:

- Android Studio 
- Android SDK
- Java 

## Algorithm

The main algorithm used in this tool is the trapezoidal rule. It is employed to calculate the rotation covered by the patient's leg based on the sensor data collected from the accelerometer and gyroscope. The algorithm involves the following steps:

1. Data Collection: The tool collects sensor data from the accelerometer and gyroscope sensors in the patient's device.
2. Integration: The collected data is processed using the trapezoidal rule to integrate the angular velocity values over time.
3. Step Detection: The tool detects steps using the integrated data by applying threshold-based algorithms.
4. Stride and Step Length Calculation: Based on the detected steps, the tool calculates the stride length and step length using appropriate formulas.
5. Comparison and Diagnosis: The calculated measurements are compared with the reference values for Parkinson's disease patients. The tool provides an analysis and diagnosis based on the comparison results.

## Usage

To use this tool, follow these steps:

1. Install Android Studio and the necessary dependencies.
2. Open the project in Android Studio.
3. Connect a compatible Android device to your computer.
4. Build and deploy the application to the connected device.
5. Launch the application on the device.
6. Follow the on-screen instructions to collect sensor data and perform the diagnosis.

## Contributing

Contributions to this project are welcome. If you encounter any issues or have suggestions for improvements, please submit them as GitHub issues.



