export const AVATAR_KEYS = [
  'ball-classic',
  'ball-cork',
  'player-red-1',
  'player-red-2',
  'player-blue-1',
  'player-blue-2',
  'table-classic',
  'table-top',
  'beer-mug',
  'beer-bottle',
  'trophy-gold',
  'trophy-silver',
  'glove-red',
  'glove-blue',
  'whistle-gold',
  'foosball-rod',
  'handle-wood',
  'handle-rubber',
  'score-counter',
  'snack-pretzel',
  'snack-pizza',
  'jersey-red',
  'jersey-blue',
  'crown'
] as const;

export type AvatarKey = typeof AVATAR_KEYS[number];
