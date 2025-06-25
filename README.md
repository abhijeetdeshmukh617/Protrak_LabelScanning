# deviceonboarder

LabelScan

## Installation

```sh
npm install deviceonboarder
```

## Usage


```js
import { startScan } from 'deviceonboarder';

// ...
const templateJson = JSON.stringify(template.template.label);
 const result = await startScan(templateJson, 5);
 //templateJson is the template label json array
 //5 is the delat time before the scan starts
```


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
