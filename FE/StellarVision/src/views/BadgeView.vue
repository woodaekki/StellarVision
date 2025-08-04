<template>
  <div class="badge-wrapper">
    <div class="badge-container" ref="containerRef">
      <Badge
        v-for="badge in badgeList"
        :key="badge.id"
        :badge="badge"
        @click="(event) => handleBadgeClick(event, badge)"
      />
    </div>

    <div
      v-if="selectedBadge"
      class="tooltip"
      :class="tooltipPosition"
      :style="{ top: tooltipY + 'px', left: tooltipX + 'px' }"
    >
      <h3>{{ selectedBadge.name }}</h3>
      <p>{{ selectedBadge.description }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Badge from '@/components/badge/Badge.vue'

const badgeList = ref([
  {
    id: 1,
    name: 'Aries',
    description: 'Aries represents the golden ram sent by Zeus to rescue Phrixus and Helle. The ram flew them across the sea, but Helle fell and drowned. Phrixus reached safety and sacrificed the ram in gratitude, hanging its golden fleece in a sacred grove.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 2,
    name: 'Taurus',
    description: 'Taurus is linked to the myth of Zeus transforming into a white bull. He abducted Europa, a Phoenician princess, and carried her across the sea to Crete. There, he revealed his divine form and made her the queen of Crete.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 3,
    name: 'Gemini',
    description: 'Gemini represents the twin brothers Castor and Pollux. One was mortal and the other divine, but they shared a deep bond. When Castor died, Pollux asked Zeus to let them stay together forever, and they were placed in the sky as the Gemini constellation.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 4,
    name: 'Cancer',
    description: 'Cancer was the giant crab that served Hera during Heracles’ battle with the Hydra. It tried to help by pinching Heracles, but he crushed it. In gratitude, Hera placed the crab in the sky as the constellation Cancer.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 5,
    name: 'Leo',
    description: 'Leo represents the Nemean Lion, a beast Heracles had to defeat as one of his twelve labors. Its hide was impervious to weapons, so Heracles strangled it with his bare hands. Zeus honored the lion by placing it among the stars.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 6,
    name: 'Virgo',
    description: 'Virgo is often associated with Astraea, the goddess of innocence and purity. She lived among humans during the Golden Age but fled to the heavens as humanity grew corrupt. She became the constellation Virgo, watching over the world from the sky.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 7,
    name: 'Libra',
    description: 'Libra symbolizes balance and justice, often linked to Themis, the Greek goddess of law. She held the scales of justice and ensured order among gods and mortals. The constellation represents fairness and the weighing of souls.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 8,
    name: 'Scorpio',
    description: 'Scorpio tells the tale of Orion, a great hunter who boasted he could kill all beasts. The Earth goddess sent a scorpion to stop him, and it succeeded. Zeus placed both Orion and the scorpion in the sky, but on opposite sides to avoid further conflict.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 9,
    name: 'Sagittarius',
    description: 'Sagittarius is associated with the centaur Chiron, a wise and kind teacher. Though immortal, he was wounded and could not heal. Wishing to end his suffering, he gave up his immortality, and Zeus placed him in the sky as Sagittarius.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 10,
    name: 'Capricorn',
    description: 'Capricorn is linked to the goat-fish deity Pan. When the monster Typhon attacked the gods, Pan dove into the Nile, his lower half becoming a fish. His cleverness and bravery earned him a place among the stars.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 11,
    name: 'Aquarius',
    description: 'Aquarius is often connected to Ganymede, a beautiful mortal youth. Zeus abducted him to serve as cupbearer to the gods on Mount Olympus. Honoring his role, Zeus placed him in the heavens as Aquarius, the water-bearer.',
    imageName: 'placeholder',
    collectCount: 0
  },
  {
    id: 12,
    name: 'Pisces',
    description: 'Pisces symbolizes the story of Aphrodite and her son Eros. When fleeing the monster Typhon, they transformed into fish and tied themselves together with rope. Their devotion was immortalized as the constellation Pisces.',
    imageName: 'placeholder',
    collectCount: 0
  }
]);

const selectedBadge = ref(null)
const tooltipX = ref(0)
const tooltipY = ref(0)
const tooltipPosition = ref('right')
const containerRef = ref(null)

function handleBadgeClick(event, badge) {
  selectedBadge.value = badge

  const targetRect = event.currentTarget.getBoundingClientRect()
  const wrapperRect = containerRef.value.getBoundingClientRect()

  const clickX = targetRect.left + targetRect.width / 2 - wrapperRect.left
  const clickY = targetRect.top - wrapperRect.top + window.scrollY

  const wrapperMid = wrapperRect.width / 2

  if (clickX > wrapperMid) {
    tooltipX.value = clickX - 120
    tooltipPosition.value = 'left'
  } else {
    tooltipX.value = clickX + 40
    tooltipPosition.value = 'right'
  }

  tooltipY.value = clickY
}

</script>

<style scoped>
.badge-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  padding: 1rem;
  overflow-x: hidden;
  position: relative;
}

.badge-wrapper {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.tooltip {
  position: absolute;
  max-width: 200px;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 10px;
  padding: 0.75rem;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  z-index: 10;
  white-space: normal;
}

.tooltip.right::after,
.tooltip.left::after {
  content: '';
  position: absolute;
  top: 1rem;
  border: 8px solid transparent;
}

.tooltip.right::after {
  left: -16px;
  border-right-color: #ccc;
}

.tooltip.left::after {
  right: -16px;
  border-left-color: #ccc;
}
</style>
