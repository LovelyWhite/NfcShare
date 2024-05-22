const aedes = require('aedes')()
const httpServer = require('http').createServer()
const ws = require('websocket-stream')
const port = 8888

aedes.on('client', function (client) {
  console.log('Client Connected: \x1b[33m' + (client ? client.id : client) + '\x1b[0m', 'to broker', aedes.id);
});

aedes.on('clientDisconnect', function (client) {
  console.log('Client Disconnected: \x1b[31m' + (client ? client.id : client) + '\x1b[0m', 'to broker', aedes.id);
})

ws.createServer({ server: httpServer }, aedes.handle)

httpServer.listen(port, function () {
  console.log('server started and listening on port ', port)
})
