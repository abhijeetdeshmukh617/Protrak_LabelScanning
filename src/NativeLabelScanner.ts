import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  startScan(templateJson: string, delayTime: number,   caputureCount: number): Promise<string>
  scanQRBarcode(caputureCount: number): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('LabelScanner');
