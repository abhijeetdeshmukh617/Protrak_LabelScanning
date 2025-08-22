import LabelScanner from './NativeLabelScanner';

export function multiply(a: number, b: number): number {
  return LabelScanner.multiply(a, b);
}

export function startScan(
  templateJson: string,
  delayTime: number,
  caputureCount: number
): Promise<string> {
  return LabelScanner.startScan(templateJson, delayTime, caputureCount);
}

export function scanQRBarcode(caputureCount: number): Promise<string> {
  return LabelScanner.scanQRBarcode(caputureCount);
}