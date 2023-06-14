const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/socket");

webSocket.onmessage = (message) => {
    console.log(`[WEBSOCKET - MESSAGE]`, message);
    message = JSON.parse(message.data);
    console.log(message);
    switch(message.type){
        case 'game_start':
            boardSize = message.roomSize;
            createBoard(boardSize);
            
            if(message.symbol == 88){
                gameStartX(message);
                player = 'x';
                $('#figure').html('<i class="fa-solid fa-xmark" style="color:#39d17d"></i>');
                if(message.othername.startsWith('[SI]'))
                    $('#info').html(' przeciwko <span style="color:#5bc0de">SI</span>')
                winningMessageElement.classList.remove('show');
            }else{
                gameStartO(message);
                player = 'o';
                $('#figure').html('<i class="fa-regular fa-circle" style="color:#5bc0de"></i>');
                winningMessageTextElement.innerText = 'Oczekiwanie na pierwszy ruch przeciwnika...';
            }
            
            
            break;
        case 'game_move':
            winningMessageElement.classList.remove('show');
            console.log("Move:", message);
            gameMove(message);
            break;
        case 'game_end':
            gameStatus = false;
            console.log("End:", message);
            gameEnd(message);
            break;
        case 'game_error':
            if(message.value == 'The other player has left!' && gameStatus)
                gameStop();
            break;
    }
};

webSocket.onopen = (event) => {
    console.log(`[WEBSOCKET - OPEN]`, event);
};

webSocket.onerror = (event) => {
    console.log(`[WEBSOCKET - ERROR]`, event);
};

webSocket.onclose = (event) => {
    console.log(`[WEBSOCKET - CLOSE]`, event);
};

let moves = 1;
let circleTurn;
let player;
let boardSize;
let gameStatus = true;
const X_CLASS = 'x';
const CIRCLE_CLASS = 'circle';
const board = document.getElementById('board')
const winningMessageElement = document.getElementById('winningMessage')
const restartButton = document.getElementById('restartButton')
const winningMessageTextElement = document.querySelector('[data-winning-message-text]')

//console.log(cellElements, board, winningMessageElement, restartButton, winningMessageTextElement);

const createBoard = (size) => {
    boardSize = size;
    for(let x = 0; x < size; x++){
        for(let y = 0; y < size; y++){
            let newDiv = document.createElement("div");
            $(newDiv).attr('data-cell', '');
            $(newDiv).attr('x', x);
            $(newDiv).attr('y', y);
            $(newDiv).attr('id', (x * size) + (y + 1));
            $(newDiv).addClass('cell');
            $(newDiv).html('<span class="moveNumber"></span>');
            board.appendChild(newDiv);
            board.style = `grid-template-columns: repeat(${size}, auto);`
        }
    }
}

const gameStartX = (message) => {
    const cellElements = document.querySelectorAll('[data-cell]');
    
    const handleClick = (e) => {
        const cell = e.target
        console.log(cell);
        if(circleTurn || (cell.classList.contains(CIRCLE_CLASS) || cell.classList.contains(X_CLASS)))
          return;
        
        webSocket.send(JSON.stringify({x: $(cell).attr('x'), y: $(cell).attr('y')}));
    }
    console.log(board);
    board.classList.remove('boardBlocked');
    circleTurn = false
    cellElements.forEach(cell => {
        cell.classList.remove(X_CLASS)
        cell.classList.remove(CIRCLE_CLASS)
        cell.removeEventListener('click', handleClick)
        cell.addEventListener('click', handleClick)
    })
    board.classList.add(X_CLASS);
}

const gameStartO = (message) => {
    const cellElements = document.querySelectorAll('[data-cell]');
    
    const handleClick = (e) => {
        const cell = e.target
        if(!circleTurn || (cell.classList.contains(CIRCLE_CLASS) || cell.classList.contains(X_CLASS)))
          return;
        
        webSocket.send(JSON.stringify({x: $(cell).attr('x'), y: $(cell).attr('y')}));
    }

    console.log(board);
    board.classList.add('boardBlocked');
    circleTurn = false;
    cellElements.forEach(cell => {
        cell.classList.remove(X_CLASS)
        cell.classList.remove(CIRCLE_CLASS)
        cell.removeEventListener('click', handleClick)
        cell.addEventListener('click', handleClick)
    })
 
    board.classList.add(CIRCLE_CLASS);
}

const gameMove = (message) => {
    const symbol = message.symbol;
    const x = message.x;
    const y = message.y;
    const id = (x * boardSize) + (y + 1);
    if(symbol == 88)
        placeMark(id, X_CLASS);
    else
        placeMark(id, CIRCLE_CLASS);
    swapTurns(symbol);
}

const placeMark = (cellId, currentClass) => {
    const cell = document.getElementById(new String(cellId));
    $(cell).find('.moveNumber').html("" + moves++);
    cell.classList.add(currentClass)
}

const swapTurns = (symbol) => {
    circleTurn = !circleTurn
    if(player == 'x'){
        if(circleTurn)
            board.classList.add('boardBlocked');
        else
            board.classList.remove('boardBlocked');
    }else{
        if(!circleTurn)
            board.classList.add('boardBlocked');
        else
            board.classList.remove('boardBlocked');
    }
    
}

const markWin = (message) => {
    if(message.x1 == message.x2){ //horizontal line
        const firstY = message.y1 < message.y2 ? message.y1 : message.y2;
        const lastY = message.y1 > message.y2 ? message.y1 : message.y2;
        for(let y = firstY; y <= lastY; y++){
            const id = (message.x1 * boardSize) + (y + 1);
            const cell = document.getElementById(new String(id));
            cell.classList.add("won");
        }
    }else if (message.y1 == message.y2){ //vertical line
        const firstX = message.x1 < message.x2 ? message.x1 : message.x2;
        const lastX = message.x1 > message.x2 ? message.x1 : message.x2;
        for(let x = firstX; x <= lastX; x++){
            const id = (x * boardSize) + (message.y1 + 1);
            const cell = document.getElementById(new String(id));
            cell.classList.add("won");
        }
    }else{ //diagonal line
        let firstX = message.x1 < message.x2 ? {cord: message.x1, w: '1'} : {cord: message.x2, w: '2'};
        let lastX = message.x1 > message.x2 ? {cord: message.x1, w: '1'} : {cord: message.x2, w: '2'};
        let firstY = message.y1 < message.y2 ? {cord: message.y1, w: '1'} : {cord: message.y2, w: '2'};
        let lastY = message.y1 > message.y2 ? {cord: message.y1, w: '1'} : {cord: message.y2, w: '2'};
        
        
        if(parseInt(message[`y${firstX.w}`]) > parseInt(message[`y${lastX.w}`])){//left to right
            console.log("right to left", firstX, firstY, lastX, lastY);
            for(let x = firstX.cord, y = lastY.cord ; x <= lastX.cord; x++, y--){
                const id = (x * boardSize) + (y + 1);
                const cell = document.getElementById(new String(id));
                console.log(id, cell);
                cell.classList.add("won");
            }
        }else{ //right to left
            console.log("left to right", firstX, firstY, lastX, lastY);
            for(let x = firstX.cord, y = firstY.cord ; x <= lastX.cord; x++, y++){
                const id = (x * boardSize) + (y + 1);
                const cell = document.getElementById(new String(id));
                console.log(id, cell);
                cell.classList.add("won");
            }
        }
        
    
    }
}

const gameEnd = (message) => {
    if(!message.isDraw){
        if(message.winner.startsWith('[SI]'))
            winningMessageTextElement.innerHTML = `<span style="color:#5bc0de">${message.winner}</span> wygrywa!`;
        else
            winningMessageTextElement.innerText = `${message.winner} wygrywa!`;
        markWin(message);
    }else{
        winningMessageTextElement.innerText = `REMIS!`;
    }   
    winningMessageElement.classList.add('show');
}

const gameStop = () => {
    winningMessageTextElement.innerText = `Twój przeciwnik opuścił rozgrywkę :(`;
    winningMessageElement.classList.add('show');
}