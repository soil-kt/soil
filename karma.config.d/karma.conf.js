// Workaround: "Error: Timeout of 2000ms exceeded. For async tests and hooks, ensure "done()" is called; if returning a Promise, ensure it resolves."
// https://stackoverflow.com/questions/75471611/kotlin-javascript-karma-test-fails
config.client.mocha = config.client.mocha || {}
config.client.mocha.timeout = 10000
