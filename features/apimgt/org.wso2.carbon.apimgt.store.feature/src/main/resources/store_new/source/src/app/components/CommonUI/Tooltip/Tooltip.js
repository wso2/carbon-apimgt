import './index.css'

import React, { PropTypes } from 'react'
import classNames from 'classnames'

class Tooltip extends React.Component{
  static propTypes = {
    placement: PropTypes.oneOf(['top', 'right', 'bottom', 'left']),
    theme: PropTypes.oneOf(['black', 'grey', 'blue', 'green', 'yellow', 'red']),
    trigger: PropTypes.element,
  }

  static defaultProps = {
    placement: 'right',
    theme: 'black',
  }

  render() {
    const { className, placement, theme, trigger } = this.props
    const placementStyles = {}

    if (trigger) {
      const triggerRect = trigger.getBoundingClientRect()

      switch (placement) {
        case 'top':
          placementStyles.left = triggerRect.left + (triggerRect.right - triggerRect.left) / 2
          placementStyles.top = triggerRect.top
          break

        case 'bottom':
          placementStyles.left = triggerRect.left + (triggerRect.right - triggerRect.left) / 2
          placementStyles.top = triggerRect.bottom
          break

        case 'right':
          placementStyles.left = triggerRect.right
          placementStyles.top = triggerRect.top + (triggerRect.bottom - triggerRect.top) / 2
          break

        case 'left':
          placementStyles.left = triggerRect.left
          placementStyles.top = triggerRect.top + (triggerRect.bottom - triggerRect.top) / 2
          break
      }
    }

    return (
      <div
        className={ classNames('tooltip', 'tooltip-' + theme, trigger && 'tooltip-' + placement, className) }
        style={ placementStyles }
      >
        <div className="tooltip_arrow" />
        <div className="tooltip_inner">
          { this.props.children }
        </div>
      </div>
    )
  }
}

export default Tooltip