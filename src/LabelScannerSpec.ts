import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  //startScan(templateJson: string, delayTime: number): string;
  startScan(templateJson: string, delayTime: number): Promise<string>;
  scanBarcodeQRcode(delayTime: number): Promise<string>;
  scanQrBarCode(a: number, b: number): number;
}

export default TurboModuleRegistry.getEnforcing<Spec>('LabelScanner');
