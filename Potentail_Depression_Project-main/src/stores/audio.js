// stores/audio.js
import { defineStore } from 'pinia'

export const useAudioStore = defineStore('audio', {
    state: () => ({
        isPlaying: false,
        audioElements: new Map() // 使用Map来管理音频元素
    }),
    actions: {
        registerAudioElement(id, element) {
            this.audioElements.set(id, element)
        },

        playBubbleSound(type) {
            const soundId = type === 'many' ? 'many-bubbles-sound' : `bubble-sound${Math.floor(Math.random() * 2) + 1}`
            const sound = this.audioElements.get(soundId)
            if (sound) {
                sound.currentTime = 0
                sound.play().catch(err => {
                    console.error(`${type === 'many' ? '多泡泡' : '单泡泡'}音效播放失败:`, err)
                })
            }
        },

        stopAll() {
            this.audioElements.forEach((el, id) => {
                if (el) {
                    el.pause()
                    el.currentTime = 0
                    // 不要移除DOM元素，只是停止播放
                }
            })
            this.isPlaying = false
            console.log('All audio stopped')
        },

        // 清理特定音频元素
        removeAudioElement(id) {
            const el = this.audioElements.get(id)
            if (el) {
                el.pause()
                el.currentTime = 0
            }
            this.audioElements.delete(id)
        }
    }
})