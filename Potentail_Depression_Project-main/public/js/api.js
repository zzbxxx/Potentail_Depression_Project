document.addEventListener('DOMContentLoaded', () => {
    const bgMusic = document.getElementById('bg-music');
    const audioControl = document.getElementById('audio-control');
    const playIcon = document.querySelector('.play-icon');

    function updateAudioUI(isPlaying) {
        if (isPlaying) {
            playIcon.classList.remove('paused');
            audioControl.title = '点击暂停';
        } else {
            playIcon.classList.add('paused');
            audioControl.title = '点击播放';
        }
    }


    function showLoading() {
        playIcon.style.display = 'none';
        const spinner = document.createElement('div');
        spinner.className = 'loading-spinner';
        audioControl.appendChild(spinner);
    }

    function hideLoading() {
        const spinner = audioControl.querySelector('.loading-spinner');
        if (spinner) spinner.remove();
        playIcon.style.display = 'block';
    }

    function playAudio() {
        showLoading();
        bgMusic.play().then(() => {
            updateAudioUI(true);
            hideLoading();
        }).catch(err => {
            console.error('播放失败:', err);
            hideLoading();
            updateAudioUI(false);
        });
    }

    bgMusic.volume = 0.4;
    bgMusic.loop = true;
    updateAudioUI(false);

    // 首次交互后允许播放（移动端限制）
    const firstInteraction = () => {
        playAudio();
        window.removeEventListener('pointerdown', firstInteraction);
        window.removeEventListener('keydown', firstInteraction);
    };
    window.addEventListener('pointerdown', firstInteraction, { once: true });
    window.addEventListener('keydown', firstInteraction, { once: true });

    bgMusic.addEventListener('play', () => updateAudioUI(true));
    bgMusic.addEventListener('pause', () => updateAudioUI(false));

    audioControl.addEventListener('click', () => {
        if (bgMusic.paused) {
            playAudio();
        } else {
            bgMusic.pause();
        }
    });



    // Three.js 场景
    const scene = new THREE.Scene();

    // 深海雾效（指数雾）
    scene.fog = new THREE.FogExp2(0x04345a, 0.06); // 颜色可调，密度可调

    const camera = new THREE.PerspectiveCamera(
        75,
        window.innerWidth / window.innerHeight,
        0.1,
        1000
    );
    const renderer = new THREE.WebGLRenderer({
        alpha: true,
        antialias: true,
        // 关键配置：允许事件穿透
        preserveDrawingBuffer: true
    });

    // 设置canvas的CSS属性
    renderer.domElement.style.position = 'fixed';
    renderer.domElement.style.top = '0';
    renderer.domElement.style.left = '0';
    renderer.domElement.style.zIndex = '-1'; // 关键：置于底层
    renderer.domElement.style.pointerEvents = 'none'; // 关键：允许事件穿透
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.outputEncoding = THREE.sRGBEncoding;
    document.getElementById('bubble-container').appendChild(renderer.domElement);

    // 光照：半球光 + 柔和定向光（模拟水体漂移）
    const hemiLight = new THREE.HemisphereLight(0x6aa9ff, 0x00152a, 0.9);
    scene.add(hemiLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.55);
    directionalLight.position.set(2, 3, 2);
    scene.add(directionalLight);

    const ambientLight = new THREE.AmbientLight(0x406080, 0.3);
    scene.add(ambientLight);

    // 泡泡集合与材质
    const bubbles = [];
    const bubbleGeometry = new THREE.SphereGeometry(0.2, 32, 32);
    function createBubbleMaterial() {
        return new THREE.MeshPhongMaterial({
            color: 0xffffff,
            transparent: true,
            opacity: 0.8,
            specular: 0x111111,
            shininess: 100,
            blending: THREE.AdditiveBlending,
            depthWrite: false
        });
    }

    // 浮游生物粒子场（无限包裹）
    const planktonCount = 1400;
    const halfRange = 70; // 粒子初始分布半径
    const wrapRange = halfRange * 2;

    const planktonGeo = new THREE.BufferGeometry();
    const pPositions = new Float32Array(planktonCount * 3);
    for (let i = 0; i < planktonCount; i++) {
        const i3 = i * 3;
        pPositions[i3 + 0] = (Math.random() * 2 - 1) * halfRange;
        pPositions[i3 + 1] = (Math.random() * 2 - 1) * halfRange;
        pPositions[i3 + 2] = (Math.random() * 2 - 1) * halfRange;
    }
    planktonGeo.setAttribute('position', new THREE.BufferAttribute(pPositions, 3));
    planktonGeo.attributes.position.usage = THREE.DynamicDrawUsage;

    const planktonMat = new THREE.PointsMaterial({
        color: 0x98d7ff,
        size: 0.075,
        sizeAttenuation: true,
        transparent: true,
        opacity: 0.65,
        depthWrite: false,
        blending: THREE.AdditiveBlending
    });

    const plankton = new THREE.Points(planktonGeo, planktonMat);
    scene.add(plankton);

    // 相机“第一人称随机游泳”路径控制
    const swim = {
        pos: new THREE.Vector3(0, -2, 0),
        vel: new THREE.Vector3(0, 0.01, 0.03),
        speed: 0.035,           // 基础速度
        targetDir: new THREE.Vector3(0, 0.15, 1).normalize(),
        lastChange: performance.now(),
        changeInterval: 2500 + Math.random() * 2500, // 下次改变方向的间隔
        boundsRadius: 45,       // 世界范围半径（越大游得越“远”）
        yMin: -18,
        yMax: 6
    };

    // 泡泡音效
    const bubbleSounds = [
        document.getElementById('bubble-sound1'),
        document.getElementById('bubble-sound2')
    ].filter(Boolean);
    const manyBubblesSound = document.getElementById('many-bubbles-sound') || null;

    bubbleSounds.forEach(s => {
        s.volume = 0.6;
        s.preload = 'auto';
    });
    if (manyBubblesSound) {
        manyBubblesSound.volume = 0.5;
        manyBubblesSound.preload = 'auto';
    }

    // 泡泡数量控制
    let activeBubbles = 0;
    const MAX_BUBBLES_FOR_SINGLE = 15;

    // 自动生成泡泡的计时器
    let lastBubbleTime = 0;
    function getRandomInterval() { return 3000 + Math.random() * 5000; }
    function getRandomBubbleCount() { return 1 + Math.floor(Math.random() * 10); }
    let nextBubbleInterval = getRandomInterval();

    function createRandomBubble() {
        const bubble = new THREE.Mesh(bubbleGeometry, createBubbleMaterial());
        // 以相机为参考随机生成
        bubble.position.x = camera.position.x + (Math.random() * 2 - 1) * 5;
        bubble.position.z = camera.position.z + (Math.random() * 2 - 1) * 5;
        bubble.position.y = camera.position.y - 5; // 从视角下方冒起

        bubble.scale.setScalar(0.5 + Math.random() * 0.5);
        bubble.userData = {
            speed: 0.02 + Math.random() * 0.03,
            wobbleSpeed: Math.random() * 0.02,
            wobbleAmount: Math.random() * 0.1
        };
        return bubble;
    }

    // 点击生成泡泡（忽略音频按钮）
    const mouse = new THREE.Vector2();
    document.addEventListener('click', (event) => {

        if (audioControl.contains(event.target)) return;

        // 将屏幕坐标转换为相机附近的相对位置
        mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
        mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;

        const bubble = new THREE.Mesh(bubbleGeometry, createBubbleMaterial());
        bubble.position.set(
            camera.position.x + mouse.x * 5,
            camera.position.y - 5,
            camera.position.z + mouse.y * 5
        );

        bubble.scale.setScalar(0.5 + Math.random() * 0.5);
        bubble.userData = {
            speed: 0.02 + Math.random() * 0.03,
            wobbleSpeed: Math.random() * 0.02,
            wobbleAmount: Math.random() * 0.1
        };

        scene.add(bubble);
        bubbles.push(bubble);
        activeBubbles++;

        // 限制总泡泡（避免无限增长）
        if (bubbles.length > 80) {
            const old = bubbles.shift();
            scene.remove(old);
            activeBubbles--;
        }

        // 播放音效
        if (activeBubbles < MAX_BUBBLES_FOR_SINGLE && bubbleSounds.length) {
            const s = bubbleSounds[Math.floor(Math.random() * bubbleSounds.length)];
            try { s.currentTime = 0; s.play(); } catch (e) { /* ignore */ }
        } else if (manyBubblesSound) {
            try { manyBubblesSound.currentTime = 0; manyBubblesSound.play(); } catch (e) { /* ignore */ }
        }
    });

    // 初始相机姿态
    camera.position.copy(swim.pos);
    camera.lookAt(swim.pos.clone().add(swim.targetDir));

    // 动画循环
    let animationFrameId;
    function animate() {
        animationFrameId = requestAnimationFrame(animate);

        const now = performance.now();

        // 随机改变前进方向（平滑转向）
        if (now - swim.lastChange > swim.changeInterval) {
            // 小幅随机偏航/俯仰
            const jitter = new THREE.Vector3(
                (Math.random() * 2 - 1) * 0.6,
                (Math.random() * 2 - 1) * 0.35,
                1
            ).normalize();
            swim.targetDir.lerp(jitter, 0.5).normalize();

            swim.changeInterval = 2400 + Math.random() * 2800;
            swim.lastChange = now;
        }

        // 边界与深度约束的引导力
        const toCenter = swim.pos.clone().multiplyScalar(-1);
        const dist = swim.pos.length();
        if (dist > swim.boundsRadius) {
            // 向中心轻拉回（越界越强）
            const pull = toCenter.normalize().multiplyScalar((dist - swim.boundsRadius) * 0.002);
            swim.targetDir.add(pull).normalize();
        }
        if (swim.pos.y < swim.yMin) {
            swim.targetDir.y += 0.02;
        } else if (swim.pos.y > swim.yMax) {
            swim.targetDir.y -= 0.02;
        }

        // 平滑速度与方向
        const dir = swim.vel.clone().normalize().lerp(swim.targetDir, 0.02).normalize();
        const speed = THREE.MathUtils.lerp(swim.vel.length(), swim.speed, 0.03);
        swim.vel.copy(dir.multiplyScalar(speed));

        // 更新位置
        swim.pos.add(swim.vel);

        // 头部轻微起伏 + 滚转
        const bob = Math.sin(now * 0.003) * 0.25;
        const roll = Math.sin(now * 0.002) * 0.03;

        camera.position.copy(swim.pos).add(new THREE.Vector3(0, bob * 0.4, 0));
        const lookTarget = swim.pos.clone()
            .add(dir.clone().multiplyScalar(5))
            .add(new THREE.Vector3(
                Math.sin(now * 0.0013) * 0.5,
                Math.cos(now * 0.0017) * 0.2,
                0
            ));
        camera.lookAt(lookTarget);
        camera.rotation.z += roll;

        // 动态光的微波纹感
        directionalLight.position.set(
            Math.sin(now * 0.0008) * 5,
            3 + Math.sin(now * 0.0013) * 0.6,
            Math.cos(now * 0.0009) * 5
        );

        // 自动生成泡泡
        if (now - lastBubbleTime > nextBubbleInterval) {
            const bubbleCount = getRandomBubbleCount();
            for (let i = 0; i < bubbleCount; i++) {
                const b = createRandomBubble();
                scene.add(b);
                bubbles.push(b);
                activeBubbles++;

                if (activeBubbles < MAX_BUBBLES_FOR_SINGLE && bubbleSounds.length) {
                    const s = bubbleSounds[Math.floor(Math.random() * bubbleSounds.length)];
                    try { s.currentTime = 0; s.play(); } catch (e) { }
                }
            }
            lastBubbleTime = now;
            nextBubbleInterval = getRandomInterval();
        }

        // 更新泡泡（随相机世界更新）
        for (let i = bubbles.length - 1; i >= 0; i--) {
            const bubble = bubbles[i];
            bubble.position.y += bubble.userData.speed;
            const t = now * bubble.userData.wobbleSpeed;
            bubble.position.x += Math.sin(t) * bubble.userData.wobbleAmount;
            bubble.position.z += Math.cos(t) * bubble.userData.wobbleAmount;
            bubble.scale.multiplyScalar(1.001);

            // 以相机为标准移除（到达视角上方）
            const tooHigh = bubble.position.y > camera.position.y + 6;
            const tooFar = bubble.position.distanceTo(camera.position) > 60;
            if (tooHigh || tooFar) {
                scene.remove(bubble);
                bubbles.splice(i, 1);
                activeBubbles = Math.max(0, activeBubbles - 1);
            }
        }

        // 浮游生物粒子无限包裹（将超出范围的点“包”回到相机附近）
        const arr = plankton.geometry.attributes.position.array;
        for (let i = 0; i < arr.length; i += 3) {
            // 分别对 x/y/z 进行包裹
            if (arr[i] - camera.position.x > halfRange) arr[i] -= wrapRange;
            else if (arr[i] - camera.position.x < -halfRange) arr[i] += wrapRange;

            if (arr[i + 1] - camera.position.y > halfRange) arr[i + 1] -= wrapRange;
            else if (arr[i + 1] - camera.position.y < -halfRange) arr[i + 1] += wrapRange;

            if (arr[i + 2] - camera.position.z > halfRange) arr[i + 2] -= wrapRange;
            else if (arr[i + 2] - camera.position.z < -halfRange) arr[i + 2] += wrapRange;
        }
        plankton.geometry.attributes.position.needsUpdate = true;

        renderer.render(scene, camera);
    }

    // 视口自适应
    window.addEventListener('resize', () => {
        camera.aspect = window.innerWidth / window.innerHeight;
        camera.updateProjectionMatrix();
        renderer.setSize(window.innerWidth, window.innerHeight);
    });

    animate();

});


