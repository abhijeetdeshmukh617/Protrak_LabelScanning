import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, PermissionsAndroid, Platform, Alert } from 'react-native';
import { multiply, startScan } from 'deviceonboarder';
import template from './template/template.json';

//const templateJson = JSON.stringify(template);
const templateJson = JSON.stringify(template.template.label);

export default function App() {
  const [scanResult, setScanResult] = useState<string>('Initializing...');
/*
  useEffect(() => {
    const runScan = async () => {
      const permissionGranted = await requestPermissions();

      if (!permissionGranted) {
        Alert.alert('Permission Denied', 'Camera and Storage permissions are required to proceed.');
        setScanResult('Permission denied');
        return;
      }

      try {
        const result = await startScan(templateJson, 5);
        setScanResult(result);
      } catch (error) {
        console.error('Scan error:', error);
        setScanResult('Scan failed');
      }
    };

    runScan();
  }, []);*/
  useEffect(() => {
    const runScan = async () => {
      const permissionGranted = await requestPermissions();

      if (!permissionGranted) {
        Alert.alert('Permission Denied', 'Camera and Storage permissions are required to proceed.');
        setScanResult('Permission denied');
        return;
      }
      try {
        const result = await startScan(templateJson, 5);
        console.error('RN App result call back:', result);
        setScanResult(result);
      } catch (e) {
        console.error('Scan error:', e);
        setScanResult('Scan failed');
      }
    };
  
    // ✅ Ensure context is ready
    setTimeout(runScan, 500); // Delay ensures RN context is stable
  }, []);
  

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Scan Result</Text>
      <Text style={styles.result}>{scanResult}</Text>
    </View>
  );
}

// Request Camera & Storage permissions
async function requestPermissions(): Promise<boolean> {
  if (Platform.OS !== 'android') return true;

  try {
    const granted = await PermissionsAndroid.requestMultiple([
      PermissionsAndroid.PERMISSIONS.CAMERA,
      PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
      PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
    ]);

    return (
      granted['android.permission.CAMERA'] === PermissionsAndroid.RESULTS.GRANTED
     // && granted['android.permission.READ_EXTERNAL_STORAGE'] === PermissionsAndroid.RESULTS.GRANTED
    );
  } catch (err) {
    console.warn('Permission error:', err);
    return false;
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: 'center',
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 22,
    fontWeight: '600',
    marginBottom: 12,
    textAlign: 'center',
  },
  result: {
    fontSize: 18,
    color: '#333',
    textAlign: 'center',
  },
});
