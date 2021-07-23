const RAIL_DATA_URL = '/api/rails/'
const SIGNAL_DATA_URL = '/api/signals/'

let globalScale = 1.0;

async function updateRail(svg, viewBoxChange) {
    let minX = 0;
    let minZ = 0;
    let maxX = 0;
    let maxZ = 0;
    let marginX = window.innerWidth / 10;
    let marginZ = window.innerHeight / 10;
    let scale = 1;
    let signals = document.getElementById("signals");
    if (signals != null) {
        signals.innerHTML = ""
    } else {
        signals = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        signals.id = "signals"
        svg.appendChild(signals)
    }

    Promise.resolve()
        .then(await fetch(RAIL_DATA_URL)
            .then(res => res.json())
            .then(json => {
                let xCoords = [];
                let zCoords = [];
                json.forEach(railCore => {
                    railCore["railMaps"].forEach(railMap => {
                        let startRP = railMap["startRP"];
                        if (startRP != null) {
                            xCoords.push(startRP["posX"])
                            zCoords.push(startRP["posZ"])
                        }

                        let endRP = railMap["endRP"];
                        if (endRP != null) {
                            xCoords.push(endRP["posX"])
                            zCoords.push(endRP["posZ"])
                        }
                    });
                });
                minX = Math.min(...xCoords)
                maxX = Math.max(...xCoords)
                minZ = Math.min(...zCoords)
                maxZ = Math.max(...zCoords)
                let scaleX = window.innerWidth * 4 / 5 / (maxX - minX)
                let scaleZ = window.innerHeight * 4 / 5 / (maxZ - minZ)
                scale = Math.min(scaleX, scaleZ)

                document.querySelectorAll("[id^='rail']").forEach(group => group.id += "flag")

                json.forEach(railCore => {
                    let pos = railCore["pos"];
                    let id = "rail," + pos[0] + "," + pos[1] + "," + pos[2] + ","
                    let isTrainOnRail = railCore["isTrainOnRail"]

                    let group = document.getElementById(id + "flag")
                    if (group != null) {
                        group.setAttribute('stroke', isTrainOnRail ? 'red' : 'white')
                        group.id = id
                    } else {
                        group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
                        group.id = id
                        group.setAttribute('stroke', isTrainOnRail ? 'red' : 'white');
                        group.setAttribute('stroke-width', '1.5px');
                        railCore["railMaps"].forEach(railMap => {
                            let startRP = railMap["startRP"];
                            let endRP = railMap["endRP"];
                            if (startRP != null && endRP != null) {
                                let startPosX = startRP["posX"] - minX + marginX
                                let startPosZ = startRP["posZ"] - minZ + marginZ
                                let endPosX = endRP["posX"] - minX + marginX
                                let endPosZ = endRP["posZ"] - minZ + marginZ;
                                let line = createLine(
                                    startPosX, startPosZ,
                                    endPosX, endPosZ)
                                group.appendChild(line)
                            }
                        });
                        svg.appendChild(group)
                    }
                });

                document.querySelectorAll("[id$='flag']").forEach(group => group.remove())
            }))
        .then(await fetch(SIGNAL_DATA_URL)
            .then(res => res.json())
            .then(json => {
                json.forEach(signal => {
                    let pos = signal["pos"]
                    let rotation = signal["rotation"]
                    let blockDirection = signal["blockDirection"]
                    let cos = Math.cos((270 + rotation) * (Math.PI / 180))
                    let sin = Math.sin((270 + rotation) * (Math.PI / 180))
                    let signalLevel = signal["signalLevel"]
                    let p = (blockDirection * 90 - rotation + 360) % 360
                    let fixX = (45 < p && p < 135) ? 3 : ((225 < p && p < 315) ? -3 : 0);

                    let posX = pos[0] - minX + marginX
                    let posZ = pos[2] - minZ + marginZ
                    let id = "signal," + pos[0] + "," + pos[1] + "," + pos[2] + ","

                    let circle = this.createSignalCircle(posX, pos[1], posZ, signalLevel, fixX)

                    let group = signals.getElementById(id)
                    if (group == null) {
                        group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
                        group.id = id
                        group.appendChild(circle)

                        let line = createLineFromComponent(
                            posX + 1.5 * cos, posZ - 1.5 * sin,
                            2.5 * cos, -2.5 * sin
                        )
                        line.setAttribute('stroke', 'lightgray');
                        line.setAttribute('stroke-width', '0.5px');
                        line.setAttribute('name', 'verticalLine');
                        group.appendChild(line);

                        let baseLine = createLineFromComponent(
                            posX + 4 * cos + 1.5 * sin, posZ - 4 * sin + 1.5 * cos,
                            -3.0 * sin, -3.0 * cos
                        )
                        baseLine.setAttribute('stroke', 'lightgray');
                        baseLine.setAttribute('stroke-width', '0.5px');
                        baseLine.setAttribute('name', 'horizontalLine');
                        group.appendChild(baseLine);
                        signals.appendChild(group);
                    } else {
                        let minus = 0
                        let last;
                        let circleArray = Array.from(group.getElementsByTagName('circle'))
                        let support
                        if ((support = group.getElementsByTagName('g')[0]) == null) {
                            support = document.createElementNS('http://www.w3.org/2000/svg', 'g');
                        }
                        support.innerHTML = ""

                        circleArray.push(circle)
                        circleArray
                            .sort((a, b) => Number(a.getAttribute("yCoord")) < Number(b.getAttribute("yCoord")) ? -1 : 1)
                            .forEach((value, index) => {
                                let fixX = Number(value.getAttribute("fix"))
                                if (fixX === 0 && index === 0) {
                                    minus += (index - last) ? -1.5 : +1.5
                                    last = index
                                    fixX = circleArray[index + 1].getAttribute("fix") * -1
                                } else if (fixX !== 0 && index !== 0) {
                                    minus += (index - last) ? -1.5 : +1.5
                                    last = index
                                    fixX = 0;
                                } else if (fixX !== 0) {
                                    minus += (index - last) ? -1.5 : +1.5
                                    last = index
                                } else if (index - last) {
                                    minus -= 1.0
                                }
                                let cx = posX - (3.5 * index + minus) * cos - fixX * sin
                                let cy = posZ + (3.5 * index + minus) * sin - fixX * cos
                                value.setAttribute('cx', String(cx))
                                value.setAttribute('cy', String(cy))
                                group.appendChild(value)

                                if (fixX !== 0 || fixX === 0 && index === 0) {
                                    let polyline = document.createElementNS('http://www.w3.org/2000/svg', 'polyline');
                                    polyline.setAttribute('points',
                                        (cx + 1.5 * cos) + "," + (cy - 1.5 * sin) + " " +
                                        (cx + 3 * cos) + "," + (cy - 3 * sin) + " " +
                                        (cx + 3 * cos + fixX * sin) + "," + (cy - 3 * sin + fixX * cos)
                                    );
                                    polyline.setAttribute('stroke', 'lightgray');
                                    polyline.setAttribute('stroke-width', '0.5px');
                                    polyline.setAttribute('name', 'holizonalLine');
                                    polyline.setAttribute('fill', 'none');
                                    support.appendChild(polyline);
                                }
                            })
                        Array.from(group.getElementsByTagName('line'))
                            .filter(line => line.getAttribute("name") === "verticalLine")
                            .find(line => {
                                line.setAttribute('x1', String(posX + (1.5 - 2.5 * circleArray.length - minus) * cos));
                                line.setAttribute('y1', String(posZ - (1.5 - 2.5 * circleArray.length - minus) * sin));
                            })
                        group.appendChild(support);
                    }
                });
            }))
        .then(() => {
            svg.weight = maxX - minX
            svg.height = maxZ - minZ
            if (viewBoxChange) {
                svg.setAttribute("viewBox", "0 0 " + (window.innerWidth / scale) + " " + (window.innerHeight / scale))
                globalScale = 1 / scale
            }
        })
}


function getSignalColor(signalLevel) {
    switch (signalLevel) {
        case 0:
            return "darkslategray"
        case 1:
            return "rgb(2045,0,0)"
        case 2:
            return "rgb(255,153,0)"
        case 3:
            return "rgb(255,204,0)"
        case 4:
            return "rgb(155,255,0)"
        case 5:
            return "rgb(51,204,0)"
        default:
            return "rgb(51,102,255)"
    }
}

function createSignalCircle(posX, yCoord, posZ, signalLevel, fixX) {
    let circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    circle.setAttribute('yCoord', String(yCoord))
    circle.setAttribute('fix', String(fixX))
    circle.setAttribute('cx', String(posX))
    circle.setAttribute('cy', String(posZ))
    circle.setAttribute('r', "1.5px")
    circle.setAttribute('fill', getSignalColor(signalLevel))
    circle.setAttribute('stroke', "lightgray")
    circle.setAttribute('stroke-width', "0.5px")
    return circle
}

function createLineFromComponent(x, y, xComponent, yComponent) {
    return createLine(x, y, x + xComponent, y + yComponent)
}

function createLine(x1, y1, x2, y2) {
    let line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
    line.setAttribute('x1', String(x1));
    line.setAttribute('y1', String(y1));
    line.setAttribute('x2', String(x2));
    line.setAttribute('y2', String(y2));
    return line
}