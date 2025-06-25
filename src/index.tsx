import LabelScanner from './NativeLabelScanner';

export function multiply(a: number, b: number): number {
  return LabelScanner.multiply(a, b);
}
/*
export function startScan(templateJson: string, delayTime: number): string {
  return LabelScanner.startScan(templateJson, delayTime);
}*/

export function startScan(templateJson: string, delayTime: number): Promise<string> {
  return LabelScanner.startScan(templateJson, delayTime);
}

