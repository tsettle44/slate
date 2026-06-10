# Slate

A minimalist Android launcher and device policy layer that turns your phone into a distraction-resistant tool — hard app allowlists, no Play Store installs, and a calm text-first home screen.

## Documentation

- [Functional spec](docs/functional-spec.md)
- [Technical spec](docs/technical-spec.md)

## Android app

```bash
cd android
./gradlew assembleDebug
```

See [android/README.md](android/README.md) for manual install and Device Owner provisioning.

## Desktop companion

Install Slate on your phone from your computer with a guided setup wizard:

```bash
cd companion
npm install
npm run dev
```

Open http://localhost:5173 and connect your phone via USB. See [companion/README.md](companion/README.md) for details.

## License

MIT
