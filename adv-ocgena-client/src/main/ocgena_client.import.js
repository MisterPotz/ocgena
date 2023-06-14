require('ts-node').register({
    transpileOnly: true,
    
});
const path = require('path')
console.log("loading ocgena client import")
require(path.resolve(__dirname, 'ocgena_client.ts'));
