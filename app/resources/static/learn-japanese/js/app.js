const contentElem = document.getElementById('content');

const katakana = {
  'A': 'ア',
  'I': 'イ',
  'U': 'ウ',
  'E': 'エ',
  'O': 'オ',
  'KA': 'カ',
  'KI': 'キ',
  'KU': 'ク',
  'KE': 'ケ',
  'KO': 'コ',
  'SA': 'サ',
  'SHI': 'シ',
  'SU': 'ス',
  'SE': 'セ',
  'SO': 'ソ',
  'TA': 'タ',
  'CHI': 'チ',
  'TSU': 'ツ',
  'TE': 'テ',
  'TO': 'ト',
  'NA': 'ナ',
  'NI': 'ニ',
  'NU': 'ヌ',
  'NE': 'ネ',
  'NO': 'ノ',
  'HA': 'ハ',
  'HI': 'ヒ',
  'FU': 'フ',
  'HE': 'ヘ',
  'HO': 'ホ',
  'MA': 'マ',
  'MI': 'ミ',
  'MU': 'ム',
  'ME': 'メ',
  'MO': 'モ',
  'YA': 'ヤ',
  'YU': 'ユ',
  'YO': 'ヨ',
  'RA': 'ラ',
  'RI': 'リ',
  'RU': 'ル',
  'RE': 'レ',
  'RO': 'ロ',
  'WA': 'ワ',
  'WO': 'ヲ',
  'N': 'ン'
};

const dakutenKatakana = {
  'GA': 'ガ',
  'GI': 'ギ',
  'GU': 'グ',
  'GE': 'ゲ',
  'GO': 'ゴ',
  'ZA': 'ザ',
  'JI': 'ジ',
  'ZU': 'ズ',
  'ZE': 'ゼ',
  'ZO': 'ゾ',
  'DA': 'ダ',
  'DZI': 'ヂ',
  'DZU': 'ヅ',
  'DE': 'デ',
  'DO': 'ド',
  'BA': 'バ',
  'BI': 'ビ',
  'BU': 'ブ',
  'BE': 'ベ',
  'BO': 'ボ',
  'PA': 'パ',
  'PI': 'ピ',
  'PU': 'プ',
  'PE': 'ペ',
  'PO': 'ポ'
};

const comboKatakana = {
  'KYA': 'キヤ',
  'KYU': 'キユ',
  'KYO': 'キヨ',
  'SHA': 'シヤ',
  'SHU': 'シユ',
  'SHO': 'シヨ',
  'CHA': 'チヤ',
  'CHU': 'チユ',
  'CHO': 'チヨ',
  'NYA': 'ニヤ',
  'NYU': 'ニユ',
  'NYO': 'ニヨ',
  'HYA': 'ヒヤ',
  'HYU': 'ヒユ',
  'HYO': 'ヒヨ',
  'MYA': 'ミヤ',
  'MYU': 'ミユ',
  'MYO': 'ミヨ',
  'RYA': 'リヤ',
  'RYU': 'リユ',
  'RYO': 'リヨ',
  'GYA': 'ギヤ',
  'GYU': 'ギユ',
  'GYO': 'ギヨ',
  'JYA': 'ジヤ',
  'JYU': 'ジユ',
  'JYO': 'ジヨ',
  'DZYA': 'ヂヤ',
  'DZYU': 'ヂユ',
  'DZYO': 'ヂヨ',
  'BYA': 'ビヤ',
  'BYU': 'ビユ',
  'BYO': 'ビヨ',
  'PYA': 'ピヤ',
  'PYU': 'ピユ',
  'PYO': 'ピヨ'
};

const fullKatakana = Object.assign(katakana, dakutenKatakana, comboKatakana);

const hiragana = {
  'A': 'あ',
  'I': 'い',
  'U': 'う',
  'E': 'え',
  'O': 'お',
  'KA': 'か',
  'KI': 'き',
  'KU': 'く',
  'KE': 'け',
  'KO': 'こ',
  'SA': 'さ',
  'SHI': 'し',
  'SU': 'す',
  'SE': 'せ',
  'SO': 'そ',
  'TA': 'た',
  'CHI': 'ち',
  'TSU': 'つ',
  'TE': 'て',
  'TO': 'と',
  'NA': 'な',
  'NI': 'に',
  'NU': 'ぬ',
  'NE': 'ね',
  'NO': 'の',
  'HA': 'は',
  'HI': 'ひ',
  'FU': 'ふ',
  'HE': 'へ',
  'HO': 'ほ',
  'MA': 'ま',
  'MI': 'み',
  'MU': 'む',
  'ME': 'め',
  'MO': 'も',
  'YA': 'や',
  'YU': 'ゆ',
  'YO': 'よ',
  'RA': 'ら',
  'RI': 'り',
  'RU': 'る',
  'RE': 'れ',
  'RO': 'ろ',
  'WA': 'わ',
  'WO': 'を',
  'N': 'ん'
};

const dakutenHiragana = {
  'GA': 'が',
  'GI': 'ぎ',
  'GU': 'ぐ',
  'GE': 'げ',
  'GO': 'ご',
  'ZA': 'ざ',
  'JI': 'じ',
  'ZU': 'ず',
  'ZE': 'ぜ',
  'ZO': 'ぞ',
  'DA': 'だ',
  'DZI': 'ぢ',
  'DZU': 'づ',
  'DE': 'で',
  'DO': 'ど',
  'BA': 'ば',
  'BI': 'び',
  'BU': 'ぶ',
  'BE': 'べ',
  'BO': 'ぼ',
  'PA': 'ぱ',
  'PI': 'ぴ',
  'PU': 'ぷ',
  'PE': 'ぺ',
  'PO': 'ぽ'
};

const comboHiragana = {
  'KYA': 'きや',
  'KYU': 'きゆ',
  'KYO': 'きよ',
  'SHA': 'しや',
  'SHU': 'しゆ',
  'SHO': 'しよ',
  'CHA': 'ちや',
  'CHU': 'ちゆ',
  'CHO': 'ちよ',
  'NYA': 'にや',
  'NYU': 'にゆ',
  'NYO': 'によ',
  'HYA': 'ひや',
  'HYU': 'ひゆ',
  'HYO': 'ひよ',
  'MYA': 'みや',
  'MYU': 'みゆ',
  'MYO': 'みよ',
  'RYA': 'りや',
  'RYU': 'りゆ',
  'RYO': 'りよ',
  'GYA': 'ぎや',
  'GYU': 'ぎゆ',
  'GYO': 'ぎよ',
  'JYA': 'じや',
  'JYU': 'じゆ',
  'JYO': 'じよ',
  'DZYA': 'ぢや',
  'DZYU': 'ぢゆ',
  'DZYO': 'ぢよ',
  'BYA': 'びや',
  'BYU': 'びゆ',
  'BYO': 'びよ',
  'PYA': 'ぴや',
  'PYU': 'ぴゆ',
  'PYO': 'ぴよ'
};

const fullHiragana = Object.assign(hiragana, dakutenHiragana, comboHiragana);

const syllabaries = { 'Katakana': fullKatakana, 'Hiragana': fullHiragana };

init();

function getParams(query) {
  if (!query) {
    return { };
  }

  return (/^[?#]/.test(query) ? query.slice(1) : query)
    .split('&')
    .reduce(function (params, param) {
      var [ key, value ] = param.split('=');
      params[key] = value ? decodeURIComponent(value.replace(/\+/g, ' ')) : '';
      return params;
    }, { });
}

function init() {
  document.getElementsByTagName('h1')[0].onclick = function() {
    setQuery({});
    return promptForTest()
  };
  const params = getParams(window.location.search);
  if (params.syllabaryName && params.question && params.answer && params.options) {
    poseQuestion(questionFromParams(params), { correct: 0, total: 0 });
  }
  else if (params.syllabaryName) {
    startNewRound(syllabaryName, { correct: 0, total: 0 });
  }
  else {
    promptForTest();
  }
}

function questionFromParams(params) {
  return Object.assign(params, { options: params.options.split(',') });
}

function setQuery(query) {
  if (query && history.pushState) {
    const queryString = Object.keys(query).reduce(function (acc, key, index) {
      const prefix = (index === 0) ? '?' : '&';
      const value = encodeURIComponent(query[key]);
      return acc + `${prefix}${key}=${value}`;
    }, '');
    const newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + queryString;
    window.history.pushState({ path: newurl }, '', newurl);
  }
}

function promptForTest() {
  contentElem.innerHTML = renderTestSelector();
}

function chooseTest(syllabaryName) {
  setQuery({ syllabaryName: syllabaryName });
  startNewRound(syllabaryName, { correct: 0, total: 0 });
}

function newQuestion(syllabaryName) {
  const syllabary = syllabaries[syllabaryName];
  const dataSet = randomItem([syllabary, invert(syllabary)]);
  const answer = randomItem(Object.keys(dataSet));
  const question = dataSet[answer];
  const options = shuffle(randomItems(removeItem(Object.keys(dataSet), answer), 3).concat([answer]));
  return { question: question, options: options, answer: answer, syllabaryName: syllabaryName };
}

function startNewRound(syllabaryName, score) {
  poseQuestion(newQuestion(syllabaryName), score);
}

function poseQuestion(question, score) {
  setQuery(question);
  contentElem.innerHTML = renderTest(question, score);
}

function invert(obj) {
  return Object.keys(obj).reduce(function (inverted, key) {
    return Object.assign(inverted, { [obj[key]]: key });
  }, {});
}

function removeItem(items, item) {
  return items.filter(function(i) { return i !== item; });
}

function randomItems(items, number) {
  return items.reduce(function (acc) {
    return acc.concat([randomItem(diff(items, acc))]);
  }, []).slice(0, number);
}

function diff(as, bs) {
  return as.filter(function (a) {
    return bs.indexOf(a) < 0;
  });
}

function randomItem(items) {
  return items[getRandomInt(0, items.length)];
}

function getRandomInt(min, max) {
  const upper = Math.ceil(min);
  const lower = Math.floor(max);
  return Math.floor(Math.random() * (upper - lower)) + lower;
}

function shuffle(items) {
  return randomItems(items, items.length);
}

function choose(correctAnswer, choice, syllabaryName, score) {
  var newScore = {};
  if (choice !== correctAnswer) {
    document.getElementById(choice).className += ' wrong';
    newScore = { correct: score.correct, total: score.total + 1};
  }
  else {
    newScore = {correct: score.correct + 1, total: score.total + 1};
  }
  document.getElementById(correctAnswer).className += ' right';
  const options = document.getElementsByClassName("option");
  for (i = 0; i < options.length; i++) {
    options[i].onclick = null;
  }
  setTimeout(function() { startNewRound(syllabaryName, newScore)}, 2000);
}

function renderTest({question, answer, options, syllabaryName}, score) {
  return `<h2>${syllabaryName}</h2>
    <div id="score">${(score.total) ? `${Math.floor(100 * (score.correct / score.total))}%` : '~'}</div>
    <div id="question">${question}</div>
    <div id="answers">
      <ol id="options">
        <li id="${options[0]}" class="option" onclick="return choose('${answer}', '${options[0]}', '${syllabaryName}', { correct: ${score.correct}, total: ${score.total} });">${options[0]}</li>
        <li id="${options[1]}" class="option" onclick="return choose('${answer}', '${options[1]}', '${syllabaryName}', { correct: ${score.correct}, total: ${score.total} });">${options[1]}</li>
        <li id="${options[2]}" class="option" onclick="return choose('${answer}', '${options[2]}', '${syllabaryName}', { correct: ${score.correct}, total: ${score.total} });">${options[2]}</li>
        <li id="${options[3]}" class="option" onclick="return choose('${answer}', '${options[3]}', '${syllabaryName}', { correct: ${score.correct}, total: ${score.total} });">${options[3]}</li>
      </ol>
    </div>`;
}

function renderTestSelector() {
  return `<h2>Select a test</h2>
    <div id="menu">
      <ol id="tests">
        <li onclick="return chooseTest('Katakana');">Katakana</li>
        <li onclick="return chooseTest('Hiragana');">Hiragana</li>
      </ol>
    </div>`;
}