import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let hostName
let portNum
let timestamp = ''
let lastCommand = 'empty'

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('Connect <username>,<host>,<port> ','Connect To Server With Username,Host And Port Arguments')
  .delimiter(cli.chalk['green']('connected>'))// where things are written to the console in the javascript with the action
  .init(function (args, callback) {
    username = args.username
    hostName = args.host
    portNum= args.port
      //server = connect({ host: 'localhost', port: 1235 }, () => {
      server = connect({ host: hostName, port: portNum }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {

      this.log(Message.fromJSON(buffer).toString())

    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {

    let [ command, ...rest ] = words(input, /[^, ]+/g)

    const contents = rest.join(' ')



    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if(command === 'broadcast')
    {

    server.write(new Message({ username, command, contents}).toJSON() + '\n')
    }
    else if(command === '@')
    {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    }else if(command === 'users')
    {
      server.write(new Message({ username, command, contents}).toJSON() + '\n')
    }
    else {
      if(lastCommand === 'empty')
      {
          this.log(`A command is required`)
        }
      else{
      console.log(lastCommand);
      command = lastCommand
        server.write(new Message({ username, command, contents}).toJSON() + '\n')
      }
    }

    lastCommand = command
    console.log(lastCommand);

    callback()
  })
