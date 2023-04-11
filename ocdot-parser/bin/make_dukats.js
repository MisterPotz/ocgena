const shell = require('shelljs');

console.log(`current dir ${shell.pwd()}`)
shell.ls("./lib").filter((entry) => {
    return entry.endsWith("d.ts")
}).forEach((file) => {
    console.log(`dukatting file: ${file} `)
    shell.exec(`npx dukat -d ./dukats/ ./lib/${file}`)
})